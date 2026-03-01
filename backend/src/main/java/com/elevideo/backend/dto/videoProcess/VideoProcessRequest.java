package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.enums.Quality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "VideoProcessing.VideoProcessRequest",
        description = "Solicitud unificada para procesar videos en diferentes modos. " +
                "El campo 'processingMode' determina qué tipo de procesamiento se aplicará."
)
public record VideoProcessRequest(

        @Schema(
                description = "Modo de procesamiento a aplicar. Valores: VERTICAL (video completo vertical), " +
                        "SHORT_AUTO (short automático), SHORT_MANUAL (short con tiempos definidos).",
                example = "VERTICAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El processingMode es requerido")
        ProcessingMode processingMode,

        @Schema(
                description = "Plataforma destino para optimización (TikTok, Instagram, YouTube Shorts).",
                example = "TIKTOK",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La plataforma es requerida")
        Platform platform,

        @Schema(
                description = "Nivel de calidad del procesamiento: FAST (rápido), NORMAL (balanceado), HIGH (máxima calidad).",
                example = "NORMAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La calidad es requerida")
        Quality quality,

        @Schema(
                description = "Modo de fondo cuando el video no es 9:16: SMART_CROP (recorte inteligente), " +
                        "BLACK (barras negras), BLURRED (fondo difuminado).",
                example = "SMART_CROP",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El backgroundMode es requerido")
        BackgroundMode backgroundMode,

        @Schema(
                description = "Opciones para short manual. REQUERIDO solo si processingMode = SHORT_MANUAL. " +
                        "Especifica el segmento exacto del video a extraer.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        ShortManualOptions shortOptions,

        @Schema(
                description = "Duración deseada del short automático en segundos (5-60). " +
                        "REQUERIDO solo si processingMode = SHORT_AUTO.",
                example = "30",
                minimum = "5",
                maximum = "60",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = 5, message = "La duración mínima es 5 segundos")
        @Max(value = 60, message = "La duración máxima es 60 segundos")
        Integer shortAutoDuration,

        @Schema(
                description = "Opciones avanzadas de configuración del algoritmo de recorte y estabilización. " +
                        "Siempre opcional; se aplican valores por defecto optimizados si no se especifica.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        AdvancedOptions advancedOptions

) {

        @Schema(
                name = "VideoProcessing.VideoProcessRequest.ShortOptions",
                description = "Configuración requerida cuando el modo de procesamiento es SHORT_MANUAL. " +
                        "Define explícitamente el segmento del video original que será extraído y convertido en short. " +
                        "El sistema no realiza detección automática; los tiempos deben ser precisos y coherentes con la duración real del video."
        )
        public record ShortManualOptions(

                @Schema(
                        description = "Tiempo de inicio del segmento en segundos desde el inicio del video. " +
                                "Debe ser mayor o igual a 0.",
                        example = "15.5",
                        requiredMode = Schema.RequiredMode.REQUIRED
                )
                @NotNull(message = "El startTime es requerido para short manual")
                @Min(value = 0, message = "El startTime no puede ser negativo")
                Double startTime,

                @Schema(
                        description = "Duración del segmento a extraer en segundos. " +
                                "Debe estar entre 5 y 60 segundos.",
                        example = "30",
                        minimum = "5",
                        maximum = "60",
                        requiredMode = Schema.RequiredMode.REQUIRED
                )
                @NotNull(message = "La duración es requerida para short manual")
                @Min(value = 5, message = "La duración mínima es 5 segundos")
                @Max(value = 60, message = "La duración máxima es 60 segundos")
                Integer duration
        ) {}
}