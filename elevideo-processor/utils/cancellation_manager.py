import logging
import threading
from typing import Optional, Set

logger = logging.getLogger(__name__)


class JobCancelledException(Exception):
    def __init__(self, job_id: str):
        self.job_id = job_id
        super().__init__(f"Job {job_id} fue cancelado")


class CancellationManager:
    """Gestiona cancelaciones de jobs de forma thread-safe."""

    def __init__(self):
        self._cancelled: Set[str] = set()
        self._lock = threading.Lock()

    def request_cancellation(self, job_id: str) -> None:
        with self._lock:
            if job_id not in self._cancelled:
                self._cancelled.add(job_id)
                logger.info("Cancelación solicitada | job_id=%s", job_id)

    def is_cancelled(self, job_id: str) -> bool:
        with self._lock:
            return job_id in self._cancelled

    def remove_cancellation(self, job_id: str) -> None:
        with self._lock:
            self._cancelled.discard(job_id)

    def get_cancelled_jobs(self) -> list:
        with self._lock:
            return list(self._cancelled)

    def clear_all(self) -> None:
        with self._lock:
            self._cancelled.clear()


def check_cancellation(manager: CancellationManager, job_id: str) -> None:
    if manager.is_cancelled(job_id):
        raise JobCancelledException(job_id)


class CancellableOperation:
    """Context manager que verifica cancelación al entrar y salir."""

    def __init__(self, manager: CancellationManager, job_id: str, operation: str):
        self._manager   = manager
        self._job_id    = job_id
        self._operation = operation

    def __enter__(self):
        check_cancellation(self._manager, self._job_id)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is None:
            check_cancellation(self._manager, self._job_id)
        return False


class CancellableProgressTracker:
    """Wrapper de ProgressTracker que verifica cancelación en cada update."""

    def __init__(self, tracker, manager: CancellationManager, job_id: str):
        self._tracker = tracker
        self._manager = manager
        self._job_id  = job_id

    def update_phase(self, phase, message=None, metadata=None) -> None:
        check_cancellation(self._manager, self._job_id)
        self._tracker.update_phase(phase, message, metadata)

    def update_progress(self, percentage, message=None) -> None:
        check_cancellation(self._manager, self._job_id)
        self._tracker.update_progress(percentage, message)

    def update_frames(self, frames_processed, total_frames) -> None:
        check_cancellation(self._manager, self._job_id)
        self._tracker.update_frames(frames_processed, total_frames)

    def complete(self, success=True) -> None:
        self._tracker.complete(success)

    def get_status(self) -> dict:
        return self._tracker.get_status()

    @property
    def current_phase(self):
        return self._tracker.current_phase

    @property
    def progress_percentage(self):
        return self._tracker.progress_percentage

    @property
    def metadata(self):
        return self._tracker.metadata


_instance: Optional[CancellationManager] = None


def get_cancellation_manager() -> CancellationManager:
    global _instance
    if _instance is None:
        _instance = CancellationManager()
    return _instance
