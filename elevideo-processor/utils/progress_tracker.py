import logging
import time
from datetime import datetime
from enum import Enum
from typing import Any, Callable, Dict, Optional

logger = logging.getLogger(__name__)


class ProcessingPhase(str, Enum):
    QUEUED            = "queued"
    VALIDATING        = "validating"
    DOWNLOADING       = "downloading"
    DOWNLOAD_COMPLETE = "download_complete"
    SELECTING_SEGMENT = "selecting_segment"
    CUTTING_SEGMENT   = "cutting_segment"
    SEGMENT_COMPLETE  = "segment_complete"
    ANALYZING         = "analyzing"
    DETECTING_FACES   = "detecting_faces"
    ANALYSIS_COMPLETE = "analysis_complete"
    PROCESSING        = "processing"
    STABILIZING       = "stabilizing"
    CROPPING          = "cropping"
    ENCODING          = "encoding"
    ENCODING_COMPLETE = "encoding_complete"
    UPLOADING         = "uploading"
    UPLOAD_COMPLETE   = "upload_complete"
    CLEANING_UP       = "cleaning_up"
    COMPLETED         = "completed"
    FAILED            = "failed"


PHASE_PROGRESS: Dict[ProcessingPhase, int] = {
    ProcessingPhase.QUEUED:            0,
    ProcessingPhase.VALIDATING:        5,
    ProcessingPhase.DOWNLOADING:       10,
    ProcessingPhase.DOWNLOAD_COMPLETE: 20,
    ProcessingPhase.SELECTING_SEGMENT: 22,
    ProcessingPhase.CUTTING_SEGMENT:   28,
    ProcessingPhase.SEGMENT_COMPLETE:  33,
    ProcessingPhase.ANALYZING:         35,
    ProcessingPhase.DETECTING_FACES:   45,
    ProcessingPhase.ANALYSIS_COMPLETE: 55,
    ProcessingPhase.PROCESSING:        58,
    ProcessingPhase.STABILIZING:       62,
    ProcessingPhase.CROPPING:          65,
    ProcessingPhase.ENCODING:          70,
    ProcessingPhase.ENCODING_COMPLETE: 85,
    ProcessingPhase.UPLOADING:         88,
    ProcessingPhase.UPLOAD_COMPLETE:   95,
    ProcessingPhase.CLEANING_UP:       98,
    ProcessingPhase.COMPLETED:         100,
    ProcessingPhase.FAILED:            0,
}

PHASE_MESSAGES: Dict[ProcessingPhase, str] = {
    ProcessingPhase.QUEUED:            "Video en cola para procesarse",
    ProcessingPhase.VALIDATING:        "Validando el video...",
    ProcessingPhase.DOWNLOADING:       "Descargando video desde Cloudinary...",
    ProcessingPhase.DOWNLOAD_COMPLETE: "Video descargado correctamente",
    ProcessingPhase.SELECTING_SEGMENT: "Seleccionando segmento del video...",
    ProcessingPhase.CUTTING_SEGMENT:   "Cortando segmento del video...",
    ProcessingPhase.SEGMENT_COMPLETE:  "Segmento listo para procesar",
    ProcessingPhase.ANALYZING:         "Analizando contenido del video...",
    ProcessingPhase.DETECTING_FACES:   "Detectando rostros en el video...",
    ProcessingPhase.ANALYSIS_COMPLETE: "Análisis completado",
    ProcessingPhase.PROCESSING:        "Procesando video...",
    ProcessingPhase.STABILIZING:       "Estabilizando movimiento de cámara...",
    ProcessingPhase.CROPPING:          "Aplicando recorte inteligente...",
    ProcessingPhase.ENCODING:          "Generando video final...",
    ProcessingPhase.ENCODING_COMPLETE: "Video generado exitosamente",
    ProcessingPhase.UPLOADING:         "Subiendo video procesado...",
    ProcessingPhase.UPLOAD_COMPLETE:   "Video subido correctamente",
    ProcessingPhase.CLEANING_UP:       "Finalizando...",
    ProcessingPhase.COMPLETED:         "Video procesado exitosamente",
    ProcessingPhase.FAILED:            "Error durante el procesamiento",
}

_NOTIFY_MIN_PROGRESS_DELTA = 1
_NOTIFY_MIN_INTERVAL       = 5.0   # segundos


class ProgressTracker:

    def __init__(self, job_id: str, update_callback: Optional[Callable] = None):
        self.job_id           = job_id
        self.update_callback  = update_callback

        self.current_phase:       Optional[ProcessingPhase]           = None
        self.progress_percentage: int                                  = 0
        self.start_time:          Optional[datetime]                   = None
        self.completion_time:     Optional[datetime]                   = None
        self.phase_timestamps:    Dict[ProcessingPhase, datetime]      = {}
        self.phases_completed:    list                                  = []
        self.frames_processed:    int                                   = 0
        self.total_frames:        Optional[int]                        = None
        self.metadata:            Dict[str, Any]                       = {}

        self._last_notified_progress: int                         = -1
        self._last_notified_phase:    Optional[ProcessingPhase]   = None
        self._last_notification_time: float                       = 0.0

    def start(self) -> None:
        self.start_time = datetime.utcnow()
        self.update_phase(ProcessingPhase.QUEUED)

    def update_phase(
        self,
        phase: ProcessingPhase,
        message: Optional[str] = None,
        metadata: Optional[Dict[str, Any]] = None,
    ) -> None:
        self.current_phase       = phase
        self.progress_percentage = PHASE_PROGRESS.get(phase, 0)
        self.phase_timestamps[phase] = datetime.utcnow()

        if phase not in self.phases_completed:
            self.phases_completed.append(phase)
        if metadata:
            self.metadata.update(metadata)

        msg = message or PHASE_MESSAGES.get(phase, str(phase))
        logger.info("Progreso | job_id=%s | phase=%s | %d%% | %.1fs | %s",
                    self.job_id, phase.value, self.progress_percentage,
                    self._elapsed(), msg)

        if self._should_notify():
            self._notify(msg)

    def update_progress(self, percentage: int, message: Optional[str] = None) -> None:
        self.progress_percentage = max(0, min(100, percentage))
        if self._should_notify():
            self._notify(message)

    def update_frames(self, frames_processed: int, total_frames: int) -> None:
        self.frames_processed = frames_processed
        self.total_frames     = total_frames

        if total_frames <= 0:
            return

        pct = frames_processed / total_frames * 100
        if self.current_phase in (ProcessingPhase.ANALYZING, ProcessingPhase.DETECTING_FACES):
            progress = int(35 + pct * 0.20)
        elif self.current_phase in (ProcessingPhase.PROCESSING, ProcessingPhase.STABILIZING):
            progress = int(58 + pct * 0.12)
        else:
            return

        self.update_progress(progress, f"Procesando frames: {frames_processed}/{total_frames}")

    def complete(self, success: bool = True) -> None:
        self.completion_time = datetime.utcnow()
        if success:
            self.update_phase(ProcessingPhase.COMPLETED)
        else:
            self.current_phase       = ProcessingPhase.FAILED
            self.progress_percentage = 0

        phase   = ProcessingPhase.COMPLETED if success else ProcessingPhase.FAILED
        logger.info("Procesamiento %s | job_id=%s | total=%.2fs",
                    "completado" if success else "falló", self.job_id, self._elapsed())
        self._notify(PHASE_MESSAGES[phase], force=True)

    def get_status(self) -> Dict[str, Any]:
        elapsed = self._elapsed()
        eta     = self._eta()
        return {
            "job_id":             self.job_id,
            "phase":              self.current_phase.value if self.current_phase else None,
            "progress":           self.progress_percentage,
            "message":            PHASE_MESSAGES.get(self.current_phase, "Procesando..."),
            "elapsed_seconds":    elapsed,
            "elapsed_formatted":  _fmt(elapsed),
            "eta_seconds":        eta,
            "eta_formatted":      _fmt(eta) if eta is not None else None,
            "start_time":         self.start_time.isoformat() if self.start_time else None,
            "frames_processed":   self.frames_processed,
            "total_frames":       self.total_frames,
            "phases_completed":   [p.value for p in self.phases_completed],
            "metadata":           self.metadata,
        }

    def _should_notify(self) -> bool:
        progress_changed = abs(self.progress_percentage - self._last_notified_progress) >= _NOTIFY_MIN_PROGRESS_DELTA
        phase_changed    = self.current_phase != self._last_notified_phase
        time_elapsed     = (time.time() - self._last_notification_time) >= _NOTIFY_MIN_INTERVAL
        return progress_changed or phase_changed or time_elapsed

    def _notify(self, message: Optional[str] = None, force: bool = False) -> None:
        if not self.update_callback:
            return
        if not force and not self._should_notify():
            return
        try:
            data = self.get_status()
            if message:
                data["message"] = message
            self.update_callback(self.job_id, data)
            self._last_notified_progress  = self.progress_percentage
            self._last_notified_phase     = self.current_phase
            self._last_notification_time  = time.time()
        except Exception as e:
            logger.error("Error en callback de progreso | job_id=%s | %s", self.job_id, e)

    def _elapsed(self) -> float:
        if not self.start_time:
            return 0.0
        return ((self.completion_time or datetime.utcnow()) - self.start_time).total_seconds()

    def _eta(self) -> Optional[float]:
        if not self.progress_percentage or not self.start_time:
            return None
        if self.progress_percentage >= 100:
            return 0.0
        elapsed = self._elapsed()
        return (elapsed / self.progress_percentage) * (100 - self.progress_percentage)


def _fmt(seconds: float) -> str:
    if seconds < 60:
        return f"{int(seconds)}s"
    if seconds < 3600:
        return f"{int(seconds // 60)}m {int(seconds % 60)}s"
    return f"{int(seconds // 3600)}h {int((seconds % 3600) // 60)}m"


def create_progress_tracker(job_id: str, update_callback: Optional[Callable] = None) -> ProgressTracker:
    tracker = ProgressTracker(job_id, update_callback)
    tracker.start()
    return tracker
