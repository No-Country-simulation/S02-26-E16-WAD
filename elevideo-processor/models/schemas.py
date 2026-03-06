from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Annotated, Literal, Optional, Union

from pydantic import BaseModel, Field


SHORT_MIN_DURATION_SECONDS: int     = 5
SHORT_MAX_DURATION_SECONDS: int     = 60
SHORT_DEFAULT_DURATION_SECONDS: int = 30


class Platform(str, Enum):
    tiktok         = "tiktok"
    instagram      = "instagram"
    youtube_shorts = "youtube_shorts"


class BackgroundMode(str, Enum):
    smart_crop = "smart_crop"
    black      = "black"
    blurred    = "blurred"


class QualityLevel(str, Enum):
    fast   = "fast"
    normal = "normal"
    high   = "high"


class ProcessingMode(str, Enum):
    vertical      = "vertical"
    short_auto    = "short_auto"
    short_manual  = "short_manual"


class JobStatus(str, Enum):
    pending    = "pending"
    processing = "processing"
    completed  = "completed"
    failed     = "failed"
    cancelled  = "cancelled"


# Mapeos de valores de usuario a valores internos de config
QUALITY_TO_PRESET: dict = {
    QualityLevel.fast:   "fast",
    QualityLevel.normal: "balanced",
    QualityLevel.high:   "professional",
}

BACKGROUND_TO_CONVERSION_MODE: dict = {
    BackgroundMode.smart_crop: "smart_crop",
    BackgroundMode.black:      "full",
    BackgroundMode.blurred:    "full",
}

BACKGROUND_TO_BLUR: dict = {
    BackgroundMode.black:      False,
    BackgroundMode.blurred:    True,
    BackgroundMode.smart_crop: None,
}


class ShortManualOptions(BaseModel):
    start_time: float = Field(ge=0.0, description="Tiempo de inicio en segundos")
    duration: int = Field(
        ge=SHORT_MIN_DURATION_SECONDS,
        le=SHORT_MAX_DURATION_SECONDS,
        description=f"Duración del short ({SHORT_MIN_DURATION_SECONDS}-{SHORT_MAX_DURATION_SECONDS}s)",
    )

    model_config = {
        "json_schema_extra": {"example": {"start_time": 45.0, "duration": 30}}
    }


class AdvancedOptions(BaseModel):
    headroom_ratio:     Optional[float] = Field(default=None, ge=0.0, le=0.5)
    smoothing_strength: Optional[float] = Field(default=None, ge=0.0, le=1.0)
    max_camera_speed:   Optional[int]   = Field(default=None, ge=10, le=100)
    apply_sharpening:   Optional[bool]  = Field(default=None)
    use_rule_of_thirds: Optional[bool]  = Field(default=None)
    edge_padding:       Optional[int]   = Field(default=None, ge=0, le=50)


class BaseVideoRequest(BaseModel):
    platform:             Platform           = Field(default=Platform.tiktok)
    background_mode:      BackgroundMode     = Field(default=BackgroundMode.smart_crop)
    quality:              QualityLevel       = Field(default=QualityLevel.normal)
    cloudinary_input_url: str                = Field(description="URL del video original en Cloudinary")
    advanced_options:     Optional[AdvancedOptions] = Field(default=None)


class VerticalRequest(BaseVideoRequest):
    processing_mode: Literal[ProcessingMode.vertical] = Field(default=ProcessingMode.vertical)

    model_config = {
        "json_schema_extra": {
            "example": {
                "processing_mode":      "vertical",
                "platform":             "tiktok",
                "background_mode":      "smart_crop",
                "quality":              "normal",
                "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
            }
        }
    }


class ShortAutoRequest(BaseVideoRequest):
    processing_mode:     Literal[ProcessingMode.short_auto] = Field(default=ProcessingMode.short_auto)
    short_auto_duration: int = Field(
        default=SHORT_DEFAULT_DURATION_SECONDS,
        ge=SHORT_MIN_DURATION_SECONDS,
        le=SHORT_MAX_DURATION_SECONDS,
        description=(
            f"Duración deseada del short ({SHORT_MIN_DURATION_SECONDS}-{SHORT_MAX_DURATION_SECONDS}s). "
            "Si el video es más corto que este valor, se usa la duración completa."
        ),
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "processing_mode":      "short_auto",
                "platform":             "tiktok",
                "background_mode":      "smart_crop",
                "quality":              "normal",
                "short_auto_duration":  30,
                "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
            }
        }
    }


class ShortManualRequest(BaseVideoRequest):
    processing_mode: Literal[ProcessingMode.short_manual] = Field(default=ProcessingMode.short_manual)
    short_options:   ShortManualOptions = Field(description="Segmento a extraer: inicio y duración")

    model_config = {
        "json_schema_extra": {
            "example": {
                "processing_mode":      "short_manual",
                "platform":             "instagram",
                "background_mode":      "smart_crop",
                "quality":              "high",
                "short_options":        {"start_time": 45.0, "duration": 30},
                "cloudinary_input_url": "https://res.cloudinary.com/demo/video/upload/sample.mp4",
            }
        }
    }


# Union discriminada — único tipo que el endpoint y los validators deben importar.
# Pydantic resuelve el modelo concreto leyendo el campo `processing_mode`.
VideoProcessRequest = Annotated[
    Union[VerticalRequest, ShortAutoRequest, ShortManualRequest],
    Field(discriminator="processing_mode"),
]


class VideoProcessResponse(BaseModel):
    """Respuesta inmediata al crear un job (202 Accepted)."""

    job_id:          str            = Field(description="ID único del job")
    status:          JobStatus      = Field(description="Estado actual del job")
    message:         str            = Field(description="Mensaje descriptivo")
    processing_mode: ProcessingMode = Field(description="Modo de procesamiento solicitado")

    model_config = {
        "json_schema_extra": {
            "example": {
                "job_id":           "a3f2c1d4-8b9e-4f1a-bc23-d5e6f7a8b9c0",
                "status":           "pending",
                "message":          "El video está en cola para procesarse",
                "processing_mode":  "short_auto",
            }
        }
    }


class JobStatusResponse(BaseModel):
    job_id:                  str
    status:                  JobStatus
    message:                 str
    processing_mode:         Optional[ProcessingMode] = None
    progress:                Optional[int]            = None
    phase:                   Optional[str]            = None
    elapsed_seconds:         Optional[float]          = None
    eta_seconds:             Optional[float]          = None
    eta_formatted:           Optional[str]            = None
    output_url:              Optional[str]            = None
    thumbnail_url:           Optional[str]            = None
    preview_url:             Optional[str]            = None
    quality_score:           Optional[float]          = None
    segment_start:           Optional[float]          = None
    segment_duration:        Optional[int]            = None
    output_duration_seconds: Optional[float]          = None
    error_detail:            Optional[str]            = None
    created_at:              Optional[datetime]       = None
    completed_at:            Optional[datetime]       = None
