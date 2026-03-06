import logging
import re
from typing import Optional, Tuple
from urllib.parse import urlparse

import requests

from core.exceptions import (
    InvalidURLError,
    VideoFormatError,
    VideoSizeError,
    VideoDurationError,
    UnsupportedPlatformError,
    ValidationError,
)
from models.schemas import (
    VideoProcessRequest,
    BackgroundMode,
    QualityLevel,
    ShortAutoRequest,
    ShortManualRequest,
    SHORT_MIN_DURATION_SECONDS,
    SHORT_MAX_DURATION_SECONDS,
)

logger = logging.getLogger(__name__)


class VideoLimits:
    SUPPORTED_FORMATS    = ["mp4", "mov", "avi", "mkv", "webm"]
    MAX_SIZE_BYTES       = 500 * 1024 * 1024   # 500 MB
    MIN_DURATION_SECONDS = 3
    MAX_DURATION_SECONDS = 600                  # 10 minutos
    MIN_WIDTH            = 640
    MIN_HEIGHT           = 360
    MAX_WIDTH            = 7680                 # 8K
    MAX_HEIGHT           = 4320


class URLValidator:

    @staticmethod
    def validate_cloudinary_url(url: str) -> None:
        if not url:
            raise InvalidURLError("URL vacía")

        try:
            parsed = urlparse(url)
            if not all([parsed.scheme, parsed.netloc]):
                raise InvalidURLError("URL malformada")
        except InvalidURLError:
            raise
        except Exception as e:
            raise InvalidURLError(f"URL inválida: {e}") from e

        if "cloudinary.com" not in parsed.netloc:
            raise InvalidURLError(
                f"La URL debe ser de Cloudinary (cloudinary.com). Se recibió: {parsed.netloc}"
            )

        if "/video/" not in url and "/raw/" not in url:
            raise InvalidURLError(
                "La URL debe apuntar a un recurso de video en Cloudinary ('/video/' en la ruta)."
            )

        if not any(url.lower().endswith(f".{fmt}") for fmt in VideoLimits.SUPPORTED_FORMATS):
            logger.warning("URL sin extensión reconocida. Formatos soportados: %s",
                           ", ".join(VideoLimits.SUPPORTED_FORMATS))

    @staticmethod
    def validate_url_accessible(url: str, timeout: int = 10) -> Tuple[bool, Optional[dict]]:
        try:
            response = requests.head(url, timeout=timeout, allow_redirects=True)
            if response.status_code != 200:
                raise InvalidURLError(f"URL no accesible. Status: {response.status_code}")

            headers_info = {
                "content_type":   response.headers.get("Content-Type", ""),
                "content_length": response.headers.get("Content-Length", "0"),
            }

            if "video" not in headers_info["content_type"].lower() and \
               "octet-stream" not in headers_info["content_type"].lower():
                logger.warning("Content-Type inusual: %s", headers_info["content_type"])

            return True, headers_info

        except requests.exceptions.Timeout:
            raise InvalidURLError(f"Timeout al acceder a la URL (>{timeout}s).")
        except requests.exceptions.ConnectionError:
            raise InvalidURLError("No se pudo conectar a la URL.")
        except requests.exceptions.RequestException as e:
            raise InvalidURLError(f"Error al acceder a la URL: {e}") from e


class VideoValidator:

    @staticmethod
    def validate_video_size(size_bytes: int) -> None:
        if size_bytes <= 0:
            raise VideoSizeError("Tamaño de video inválido")
        if size_bytes > VideoLimits.MAX_SIZE_BYTES:
            raise VideoSizeError(
                f"El video es demasiado grande: {size_bytes / (1024**2):.2f}MB. "
                f"Máximo: {VideoLimits.MAX_SIZE_BYTES // (1024**2)}MB"
            )

    @staticmethod
    def validate_video_duration(duration_seconds: float) -> None:
        if duration_seconds < VideoLimits.MIN_DURATION_SECONDS:
            raise VideoDurationError(
                f"El video es demasiado corto: {duration_seconds:.1f}s. "
                f"Mínimo: {VideoLimits.MIN_DURATION_SECONDS}s"
            )
        if duration_seconds > VideoLimits.MAX_DURATION_SECONDS:
            raise VideoDurationError(
                f"El video es demasiado largo: {duration_seconds:.1f}s. "
                f"Máximo: {VideoLimits.MAX_DURATION_SECONDS}s "
                f"({VideoLimits.MAX_DURATION_SECONDS // 60} minutos)"
            )

    @staticmethod
    def validate_video_resolution(width: int, height: int) -> None:
        if width < VideoLimits.MIN_WIDTH or height < VideoLimits.MIN_HEIGHT:
            raise VideoFormatError(
                f"Resolución muy baja: {width}x{height}. "
                f"Mínimo: {VideoLimits.MIN_WIDTH}x{VideoLimits.MIN_HEIGHT}"
            )
        if width > VideoLimits.MAX_WIDTH or height > VideoLimits.MAX_HEIGHT:
            raise VideoFormatError(
                f"Resolución muy alta: {width}x{height}. "
                f"Máximo: {VideoLimits.MAX_WIDTH}x{VideoLimits.MAX_HEIGHT}"
            )


class ShortOptionsValidator:

    @staticmethod
    def validate_short_auto(target_duration: int, video_duration: Optional[float] = None) -> None:
        if not (SHORT_MIN_DURATION_SECONDS <= target_duration <= SHORT_MAX_DURATION_SECONDS):
            raise VideoDurationError(
                f"Duración del short fuera de rango ({SHORT_MIN_DURATION_SECONDS}-{SHORT_MAX_DURATION_SECONDS}s). "
                f"Se recibió: {target_duration}s"
            )
        if video_duration is not None:
            if video_duration < SHORT_MIN_DURATION_SECONDS:
                raise VideoDurationError(
                    f"Video demasiado corto para generar un short: {video_duration:.1f}s. "
                    f"Mínimo: {SHORT_MIN_DURATION_SECONDS}s"
                )
            if video_duration < target_duration:
                logger.warning("Video (%.1fs) más corto que target (%ds). Se usará duración completa.",
                               video_duration, target_duration)

    @staticmethod
    def validate_short_manual(
        start_time: float,
        duration: int,
        video_duration: Optional[float] = None,
    ) -> None:
        if start_time < 0:
            raise ValidationError(f"El tiempo de inicio no puede ser negativo: {start_time}s")

        if not (SHORT_MIN_DURATION_SECONDS <= duration <= SHORT_MAX_DURATION_SECONDS):
            raise VideoDurationError(
                f"Duración del segmento fuera de rango ({SHORT_MIN_DURATION_SECONDS}-{SHORT_MAX_DURATION_SECONDS}s). "
                f"Se recibió: {duration}s"
            )

        if video_duration is not None:
            if start_time >= video_duration:
                raise ValidationError(
                    f"Tiempo de inicio ({start_time}s) >= duración del video ({video_duration:.1f}s)."
                )
            end_time = start_time + duration
            if end_time > video_duration:
                raise VideoDurationError(
                    f"Segmento ({start_time}s + {duration}s = {end_time}s) excede el video ({video_duration:.1f}s). "
                    f"Máximo desde {start_time}s: {video_duration - start_time:.1f}s"
                )


def validate_video_request(request: VideoProcessRequest) -> dict:
    if not request.cloudinary_input_url or not request.cloudinary_input_url.strip():
        raise InvalidURLError("La URL del video es requerida")

    URLValidator.validate_cloudinary_url(request.cloudinary_input_url)

    if request.quality == QualityLevel.fast:
        logger.warning("Calidad 'fast' seleccionada — recomendado solo para pruebas.")

    if isinstance(request, (ShortAutoRequest, ShortManualRequest)) and \
       request.background_mode == BackgroundMode.blurred:
        logger.warning("Modo '%s' con fondo 'blurred' puede aumentar el tiempo de procesamiento.",
                       request.processing_mode.value)

    _, headers_info = URLValidator.validate_url_accessible(request.cloudinary_input_url)

    video_info: dict = {}
    if headers_info:
        try:
            size_bytes = int(headers_info.get("content_length", 0))
            if size_bytes > 0:
                VideoValidator.validate_video_size(size_bytes)
                video_info["size_bytes"] = size_bytes
                video_info["size_mb"]    = size_bytes / (1024 * 1024)
        except (ValueError, TypeError):
            logger.warning("No se pudo obtener el tamaño del video desde headers")

    if isinstance(request, ShortAutoRequest):
        ShortOptionsValidator.validate_short_auto(
            target_duration=request.short_auto_duration,
            video_duration=None,
        )
    elif isinstance(request, ShortManualRequest):
        ShortOptionsValidator.validate_short_manual(
            start_time=request.short_options.start_time,
            duration=request.short_options.duration,
            video_duration=None,
        )

    logger.info("Request validado | mode=%s | platform=%s | quality=%s",
                request.processing_mode.value, request.platform.value, request.quality.value)
    return video_info
