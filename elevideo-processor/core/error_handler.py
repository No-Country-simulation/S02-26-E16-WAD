import logging
import traceback
import time
from typing import Optional, Callable, Any
from functools import wraps

from core.exceptions import (
    ValidationError,
    CloudinaryError,
    VideoProcessingError,
)

logger = logging.getLogger(__name__)


class ErrorCategory:
    VALIDATION = "validation"
    NETWORK    = "network"
    CLOUDINARY = "cloudinary"
    PROCESSING = "processing"
    STORAGE    = "storage"
    SYSTEM     = "system"
    UNKNOWN    = "unknown"


def _classify(error: Exception) -> str:
    error_type = type(error).__name__.lower()
    error_msg  = str(error).lower()

    if isinstance(error, ValidationError):
        return ErrorCategory.VALIDATION
    if isinstance(error, CloudinaryError) or "cloudinary" in error_msg:
        return ErrorCategory.CLOUDINARY
    if isinstance(error, VideoProcessingError):
        return ErrorCategory.PROCESSING
    if any(x in error_type for x in ("timeout", "connection", "network")):
        return ErrorCategory.NETWORK
    if any(x in error_type for x in ("filenotfound", "permission", "ioerror")):
        return ErrorCategory.STORAGE
    if any(x in error_type for x in ("memory", "system", "os")):
        return ErrorCategory.SYSTEM

    return ErrorCategory.UNKNOWN


def _is_retryable(error: Exception) -> bool:
    category = _classify(error)

    if category in (ErrorCategory.NETWORK, ErrorCategory.CLOUDINARY):
        return True
    if category == ErrorCategory.VALIDATION:
        return False

    return any(x in str(error).lower() for x in ("timeout", "temporary", "busy"))


def _user_message(error: Exception, category: str) -> str:
    if category == ErrorCategory.VALIDATION:
        return str(error)

    if category == ErrorCategory.NETWORK:
        return "Error de conexión. Verifica tu internet e intenta de nuevo."

    if category == ErrorCategory.CLOUDINARY:
        msg = str(error).lower()
        if "not found" in msg:
            return "No se pudo encontrar el video en Cloudinary. Verifica que la URL sea correcta."
        if "unauthorized" in msg:
            return "Error de autenticación con Cloudinary. Contacta al administrador."
        return "Error al comunicarse con Cloudinary. Intenta de nuevo más tarde."

    if category == ErrorCategory.PROCESSING:
        msg = str(error).lower()
        if "face" in msg or "rostro" in msg:
            return (
                "No se pudo detectar un rostro en el video. "
                "El modo 'smart_crop' requiere al menos un rostro visible."
            )
        if "codec" in msg or "format" in msg:
            return "El formato del video podría no ser compatible."
        return "Error durante el procesamiento. Verifica que el video sea válido e intenta de nuevo."

    if category == ErrorCategory.STORAGE:
        return "Error de almacenamiento temporal. Es posible que el servidor esté sin espacio."

    if category == ErrorCategory.SYSTEM:
        return "Error del sistema. Nuestro equipo ha sido notificado."

    return "Ocurrió un error inesperado. Si el problema persiste, contacta soporte."


def retry(
    max_attempts: int = 3,
    initial_delay: float = 2.0,
    backoff: float = 2.0,
    only_retryable: bool = True,
):
    """Decorator que reintenta una función con backoff exponencial."""
    def decorator(func: Callable) -> Callable:
        @wraps(func)
        def wrapper(*args, **kwargs) -> Any:
            delay = initial_delay

            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    if only_retryable and not _is_retryable(e):
                        raise

                    if attempt == max_attempts:
                        logger.error("%s falló tras %d intentos", func.__name__, max_attempts)
                        raise

                    logger.warning(
                        "%s — intento %d/%d falló: %s. Reintentando en %.1fs",
                        func.__name__, attempt, max_attempts, e, delay,
                    )
                    time.sleep(delay)
                    delay *= backoff

        return wrapper
    return decorator


class ErrorContext:
    """Context manager que ejecuta un cleanup opcional si ocurre un error."""

    def __init__(
        self,
        operation: str,
        cleanup: Optional[Callable] = None,
        job_id: Optional[str] = None,
    ):
        self.operation = operation
        self.cleanup   = cleanup
        self.job_id    = job_id

    def __enter__(self):
        logger.info("Iniciando: %s%s", self.operation, f" | job_id={self.job_id}" if self.job_id else "")
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is None:
            return False

        logger.error("Error en '%s': %s", self.operation, exc_val)

        if self.cleanup:
            try:
                self.cleanup()
            except Exception as e:
                logger.error("Cleanup falló: %s", e)

        return False


class ErrorHandler:
    @staticmethod
    def handle(
        error: Exception,
        job_id: Optional[str] = None,
        operation: Optional[str] = None,
    ) -> dict:
        category   = _classify(error)
        retryable  = _is_retryable(error)
        user_msg   = _user_message(error, category)

        logger.error(
            "Error | job_id=%s | op=%s | category=%s | retryable=%s | %s",
            job_id or "N/A", operation or "N/A", category, retryable, error,
        )
        logger.debug("Traceback:\n%s", traceback.format_exc())

        return {
            "error_type":        type(error).__name__,
            "error_category":    category,
            "is_retryable":      retryable,
            "user_message":      user_msg,
            "technical_message": str(error),
            "job_id":            job_id,
        }
