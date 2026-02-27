package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.enums.Quality;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(
        name = "Video.VerticalCreateRequest",
        description = "Solicitud para procesar un video en formato vertical optimizado para redes sociales. Permite configurar calidad, plataforma destino y opciones avanzadas de procesamiento.",
        requiredProperties = {"platform", "quality", "backgroundMode", "processingMode"}
)
public record VideoVerticalCreateRequest(

        @Schema(
                description = "Plataforma destino para la optimización del video. Determina ajustes como resolución y proporciones.",
                example = "tiktok"
        )
        @NotNull(message = "La plataforma es requerida")
        Platform platform,

        @Schema(
                description = "Nivel de calidad del procesamiento final del video.",
                example = "normal"
        )
        @NotNull(message = "La calidad es requerida")
        Quality quality,

        @Schema(
                description = "Modo de fondo a utilizar cuando el video original no coincide con el aspecto 9:16.",
                example = "smart_crop"
        )
        @NotNull(message = "El backgroundMode es requerido")
        BackgroundMode backgroundMode,

        @Schema(
                description = "Modo de procesamiento a aplicar sobre el video.",
                example = "vertical"
        )
        @NotNull(message = "El processingMode es requerido")
        ProcessingMode processingMode,

        @Schema(
                description = "Opciones avanzadas de configuración del algoritmo de recorte y estabilización. Es opcional; si no se envía, se aplican valores por defecto optimizados."
        )
        AdvancedOptions advancedOptions

) {
}
