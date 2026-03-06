import logging
import uuid
from datetime import datetime
from typing import Dict

from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException, status

from models.schemas import (
    VideoProcessRequest,
    VideoProcessResponse,
    JobStatusResponse,
    JobStatus,
    ProcessingMode,
)
from services.video_service import VideoProcessingService
from storage.cloudinary_service import CloudinaryService
from utils.validators import validate_video_request
from core.exceptions import ValidationError, VideoProcessingError
from core.error_handler import ErrorHandler
from core.auth import TokenData, require_service_token, verify_job_ownership
from utils.cancellation_manager import get_cancellation_manager, JobCancelledException
from services.webhook_service import notify_job_completed, notify_job_failed, notify_job_cancelled

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/video", tags=["Video"])

jobs_db: Dict[str, dict] = {}
cancellation_manager = get_cancellation_manager()

cloudinary_service: CloudinaryService = None
video_service: VideoProcessingService  = None


def set_services(cloudinary_svc: CloudinaryService, video_svc: VideoProcessingService) -> None:
    global cloudinary_service, video_service
    cloudinary_service = cloudinary_svc
    video_service      = video_svc

    def _on_progress(job_id: str, data: dict) -> None:
        if job_id in jobs_db:
            jobs_db[job_id].update({
                "progress":        data.get("progress", 0),
                "message":         data.get("message", ""),
                "phase":           data.get("phase"),
                "elapsed_seconds": data.get("elapsed_seconds"),
                "eta_seconds":     data.get("eta_seconds"),
                "eta_formatted":   data.get("eta_formatted"),
            })

    video_service.set_progress_callback(_on_progress)


def _fail_job(job_id: str, user_message: str) -> None:
    jobs_db[job_id].update({
        "status":       JobStatus.failed,
        "message":      user_message,
        "error_detail": user_message,
        "progress":     0,
        "completed_at": datetime.utcnow(),
    })


def process_video_task(job_id: str, request: VideoProcessRequest) -> None:
    try:
        logger.info("Job iniciado | job_id=%s | mode=%s", job_id, request.processing_mode.value)

        jobs_db[job_id].update({"status": JobStatus.processing, "message": "Procesando el video...", "progress": 10})

        output_url, metrics = video_service.process_video(request, job_id)

        jobs_db[job_id].update({
            "status":                  JobStatus.completed,
            "message":                 "Video procesado exitosamente",
            "progress":                100,
            "output_url":              output_url,
            "thumbnail_url":           metrics.get("thumbnail_url"),
            "preview_url":             metrics.get("preview_url"),
            "quality_score":           metrics.get("overall_quality"),
            "segment_start":           metrics.get("segment_start"),
            "segment_duration":        metrics.get("segment_duration"),
            "output_duration_seconds": metrics.get("output_duration_seconds"),
            "completed_at":            datetime.utcnow(),
        })

        logger.info("Job completado | job_id=%s | url=%s", job_id, output_url)
        notify_job_completed(job_id=job_id, output_url=output_url, metrics=metrics, job_data=jobs_db[job_id])

    except JobCancelledException:
        jobs_db[job_id].update({
            "status":       JobStatus.cancelled,
            "message":      "Procesamiento cancelado por el usuario",
            "progress":     0,
            "completed_at": datetime.utcnow(),
        })
        logger.info("Job cancelado | job_id=%s", job_id)
        notify_job_cancelled(job_id=job_id, job_data=jobs_db[job_id])

    except VideoProcessingError as e:
        error_info = ErrorHandler.handle(e, job_id=job_id, operation="background_task")
        _fail_job(job_id, error_info["user_message"])
        logger.error("Job falló | job_id=%s | error=%s", job_id, error_info["user_message"])
        notify_job_failed(job_id=job_id, error_message=error_info["user_message"], job_data=jobs_db[job_id])

    except Exception as e:
        error_info = ErrorHandler.handle(e, job_id=job_id, operation="background_task")
        _fail_job(job_id, error_info["user_message"])
        logger.exception("Job falló con error inesperado | job_id=%s", job_id)
        notify_job_failed(job_id=job_id, error_message=error_info["user_message"], job_data=jobs_db[job_id])


_PROCESS_EXAMPLES = {
    "vertical": {
        "summary": "Video completo a 9:16",
        "value": {
            "processing_mode": "vertical", "platform": "tiktok",
            "background_mode": "smart_crop", "quality": "normal",
            "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
        },
    },
    "short_auto": {
        "summary": "Short automático — segmento central",
        "value": {
            "processing_mode": "short_auto", "platform": "tiktok",
            "background_mode": "smart_crop", "quality": "normal",
            "short_auto_duration": 30,
            "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
        },
    },
    "short_manual": {
        "summary": "Short manual — segmento exacto",
        "value": {
            "processing_mode": "short_manual", "platform": "instagram",
            "background_mode": "smart_crop", "quality": "high",
            "short_options": {"start_time": 45.0, "duration": 30},
            "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
        },
    },
}

_ERRORS = {
    401: {"description": "Token ausente o inválido"},
    403: {"description": "Job pertenece a otro usuario"},
    404: {"description": "Job no encontrado"},
}


@router.post(
    "/process",
    response_model=VideoProcessResponse,
    status_code=status.HTTP_202_ACCEPTED,
    summary="Crear job de procesamiento",
    responses={
        400: {"description": "Request inválido"},
        **_ERRORS,
    },
    openapi_extra={
        "requestBody": {
            "required": True,
            "content": {"application/json": {"examples": _PROCESS_EXAMPLES}},
        }
    },
)
async def process_video(
    request: VideoProcessRequest,
    background_tasks: BackgroundTasks,
    token: TokenData = Depends(require_service_token),
):
    try:
        video_info = validate_video_request(request)
    except ValidationError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception:
        logger.exception("Error inesperado durante validación")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Error al validar el request")

    job_id = str(uuid.uuid4())
    jobs_db[job_id] = {
        "job_id":                  job_id,
        "user_id":                 token.user_id,
        "status":                  JobStatus.pending,
        "message":                 "El video está en cola para procesarse",
        "processing_mode":         request.processing_mode,
        "progress":                0,
        "output_url":              None,
        "thumbnail_url":           None,
        "preview_url":             None,
        "quality_score":           None,
        "error_detail":            None,
        "segment_start":           None,
        "segment_duration":        None,
        "output_duration_seconds": None,
        "created_at":              datetime.utcnow(),
        "completed_at":            None,
        "request":                 request.model_dump(),
        "video_info":              video_info,
    }

    background_tasks.add_task(process_video_task, job_id, request)

    logger.info(
        "Job creado | job_id=%s | user_id=%s | mode=%s | platform=%s",
        job_id, token.user_id, request.processing_mode.value, request.platform.value,
    )

    return VideoProcessResponse(
        job_id=job_id,
        status=JobStatus.pending,
        message="El video está en cola para procesarse",
        processing_mode=request.processing_mode,
    )


@router.get(
    "/status/{job_id}",
    response_model=JobStatusResponse,
    summary="Consultar estado del job",
    responses=_ERRORS,
)
async def get_job_status(
    job_id: str,
    token: TokenData = Depends(require_service_token),
):
    job = jobs_db.get(job_id)
    verify_job_ownership(job, token, job_id)

    return JobStatusResponse(
        job_id=job["job_id"],
        status=job["status"],
        message=job["message"],
        processing_mode=job.get("processing_mode"),
        progress=job.get("progress"),
        phase=job.get("phase"),
        elapsed_seconds=job.get("elapsed_seconds"),
        eta_seconds=job.get("eta_seconds"),
        eta_formatted=job.get("eta_formatted"),
        output_url=job.get("output_url"),
        thumbnail_url=job.get("thumbnail_url"),
        preview_url=job.get("preview_url"),
        quality_score=job.get("quality_score"),
        segment_start=job.get("segment_start"),
        segment_duration=job.get("segment_duration"),
        output_duration_seconds=job.get("output_duration_seconds"),
        error_detail=job.get("error_detail"),
        created_at=job.get("created_at"),
        completed_at=job.get("completed_at"),
    )


@router.get(
    "/download/{job_id}",
    summary="Obtener URL de descarga",
    responses={
        400: {"description": "Video aún no disponible"},
        **_ERRORS,
    },
)
async def download_video(
    job_id: str,
    token: TokenData = Depends(require_service_token),
):
    job = jobs_db.get(job_id)
    verify_job_ownership(job, token, job_id)

    if job["status"] != JobStatus.completed:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="El video no está listo para descarga")

    return {
        "job_id":           job_id,
        "status":           "completed",
        "processing_mode":  job.get("processing_mode"),
        "video_url":        job["output_url"],
        "quality_score":    job.get("quality_score"),
        "segment_start":    job.get("segment_start"),
        "segment_duration": job.get("segment_duration"),
    }


@router.post(
    "/jobs/{job_id}/cancel",
    summary="Cancelar job en proceso",
    responses={
        400: {"description": "Job no cancelable"},
        **_ERRORS,
    },
)
async def cancel_job(
    job_id: str,
    token: TokenData = Depends(require_service_token),
):
    job = jobs_db.get(job_id)
    verify_job_ownership(job, token, job_id)

    current_status = job["status"]
    if current_status in (JobStatus.completed, JobStatus.failed, JobStatus.cancelled):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"El job ya está en estado '{current_status}' y no puede ser cancelado",
        )

    cancellation_manager.request_cancellation(job_id)
    jobs_db[job_id]["message"] = "Cancelando procesamiento..."
    logger.info("Cancelación solicitada | job_id=%s | status=%s", job_id, current_status)

    return {
        "job_id":          job_id,
        "message":         "Cancelación solicitada. El procesamiento se detendrá pronto.",
        "previous_status": current_status,
    }


@router.get("/jobs", summary="Listar jobs del usuario", tags=["Utilidades"])
async def list_jobs(token: TokenData = Depends(require_service_token)):
    user_jobs = [
        {
            "job_id":          jid,
            "status":          data["status"],
            "processing_mode": data.get("processing_mode"),
            "created_at":      data["created_at"],
            "platform":        data["request"]["platform"],
            "quality":         data["request"]["quality"],
        }
        for jid, data in jobs_db.items()
        if data.get("user_id") == token.user_id
    ]
    return {"total_jobs": len(user_jobs), "jobs": user_jobs}


@router.delete(
    "/jobs/{job_id}",
    summary="Eliminar job",
    tags=["Utilidades"],
    responses={404: {"description": "Job no encontrado"}},
)
async def delete_job(
    job_id: str,
    token: TokenData = Depends(require_service_token),
):
    job = jobs_db.get(job_id)
    verify_job_ownership(job, token, job_id)
    del jobs_db[job_id]
    logger.info("Job eliminado | job_id=%s | user_id=%s", job_id, token.user_id)
    return {"message": f"Job {job_id} eliminado exitosamente"}
