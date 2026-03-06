import logging
import os
import time
from datetime import datetime, timezone
from typing import Any, Dict

import requests
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

_WEBHOOK_URL          = os.getenv("SPRING_BOOT_WEBHOOK_URL", "")
_PROGRESS_WEBHOOK_URL = os.getenv("SPRING_BOOT_PROGRESS_WEBHOOK_URL", "")
_SERVICE_API_KEY      = os.getenv("SERVICE_API_KEY", "")

_MAX_RETRIES    = 3
_RETRY_DELAY    = 1.0
_TIMEOUT        = 10
_PROGRESS_TIMEOUT = 3

if not _WEBHOOK_URL:
    logger.warning("SPRING_BOOT_WEBHOOK_URL no configurada — notificaciones finales DESACTIVADAS")
if not _PROGRESS_WEBHOOK_URL:
    logger.warning("SPRING_BOOT_PROGRESS_WEBHOOK_URL no configurada — notificaciones de progreso DESACTIVADAS")
if not _SERVICE_API_KEY:
    logger.warning("SERVICE_API_KEY no configurada — Spring Boot rechazará los webhooks")

_HEADERS = {"Content-Type": "application/json", "X-Service-Key": _SERVICE_API_KEY}


def notify_job_completed(job_id: str, output_url: str, metrics: dict, job_data: dict) -> bool:
    if not _WEBHOOK_URL:
        return False
    payload = {
        "job_id":                  job_id,
        "status":                  "completed",
        "processing_mode":         metrics.get("processing_mode", job_data.get("request", {}).get("processing_mode")),
        "elapsed_seconds":         metrics.get("processing_total_time"),
        "phase":                   "completed",
        "completed_at":            datetime.now(timezone.utc).isoformat(),
        "output_url":              output_url,
        "thumbnail_url":           metrics.get("thumbnail_url"),
        "preview_url":             metrics.get("preview_url"),
        "quality_score":           metrics.get("overall_quality"),
        "output_duration_seconds": metrics.get("output_duration_seconds"),
        "segment_start":           metrics.get("segment_start"),
        "segment_duration":        metrics.get("segment_duration"),
        "error_detail":            None,
    }
    return _send_with_retry(job_id, payload)


def notify_job_failed(job_id: str, error_message: str, job_data: dict) -> bool:
    if not _WEBHOOK_URL:
        return False
    payload = {
        "job_id":                  job_id,
        "status":                  "failed",
        "processing_mode":         job_data.get("request", {}).get("processing_mode"),
        "elapsed_seconds":         None,
        "phase":                   "failed",
        "completed_at":            datetime.now(timezone.utc).isoformat(),
        "output_url":              None,
        "thumbnail_url":           None,
        "preview_url":             None,
        "quality_score":           None,
        "output_duration_seconds": None,
        "segment_start":           None,
        "segment_duration":        None,
        "error_detail":            error_message,
    }
    return _send_with_retry(job_id, payload)


def notify_job_cancelled(job_id: str, job_data: dict) -> bool:
    if not _WEBHOOK_URL:
        return False
    payload = {
        "job_id":                  job_id,
        "status":                  "cancelled",
        "processing_mode":         job_data.get("request", {}).get("processing_mode"),
        "elapsed_seconds":         None,
        "phase":                   "cancelled",
        "completed_at":            datetime.now(timezone.utc).isoformat(),
        "output_url":              None,
        "thumbnail_url":           None,
        "preview_url":             None,
        "quality_score":           None,
        "output_duration_seconds": None,
        "segment_start":           None,
        "segment_duration":        None,
        "error_detail":            None,
    }
    return _send_with_retry(job_id, payload)


def notify_progress(job_id: str, progress_data: Dict[str, Any]) -> bool:
    """Best-effort: no reintenta si falla."""
    if not _PROGRESS_WEBHOOK_URL:
        return False
    payload = {
        "job_id":          job_id,
        "status":          "processing",
        "progress":        progress_data.get("progress", 0),
        "phase":           progress_data.get("phase"),
        "eta_seconds":     progress_data.get("eta_seconds"),
        "elapsed_seconds": progress_data.get("elapsed_seconds"),
        "message":         progress_data.get("message", "Procesando..."),
    }
    try:
        response = requests.post(_PROGRESS_WEBHOOK_URL, json=payload, headers=_HEADERS, timeout=_PROGRESS_TIMEOUT)
        return response.status_code in (200, 201, 204)
    except (requests.Timeout, requests.ConnectionError):
        return False
    except Exception as e:
        logger.warning("Webhook de progreso error | job_id=%s | %s", job_id, e)
        return False


def _send_with_retry(job_id: str, payload: dict) -> bool:
    for attempt in range(1, _MAX_RETRIES + 1):
        try:
            response = requests.post(_WEBHOOK_URL, json=payload, headers=_HEADERS, timeout=_TIMEOUT)

            if response.status_code in (200, 201, 204):
                logger.info("Webhook enviado | job_id=%s | attempt=%d", job_id, attempt)
                return True

            if 400 <= response.status_code < 500:
                logger.error("Webhook rechazado | job_id=%s | status=%d", job_id, response.status_code)
                return False

            logger.warning("Webhook error de servidor | job_id=%s | status=%d | attempt=%d/%d",
                           job_id, response.status_code, attempt, _MAX_RETRIES)

        except requests.Timeout:
            logger.warning("Webhook timeout | job_id=%s | attempt=%d/%d", job_id, attempt, _MAX_RETRIES)
        except requests.ConnectionError:
            logger.warning("Webhook connection error | job_id=%s | attempt=%d/%d", job_id, attempt, _MAX_RETRIES)
        except Exception as e:
            logger.warning("Webhook error | job_id=%s | attempt=%d/%d | %s", job_id, attempt, _MAX_RETRIES, e)

        if attempt < _MAX_RETRIES:
            time.sleep(_RETRY_DELAY * (2 ** (attempt - 1)))

    logger.error("Webhook falló tras %d intentos | job_id=%s", _MAX_RETRIES, job_id)
    return False
