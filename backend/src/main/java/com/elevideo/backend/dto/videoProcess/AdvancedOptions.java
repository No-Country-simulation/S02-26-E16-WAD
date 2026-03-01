package com.elevideo.backend.dto.videoProcess;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(
        name = "VideoProcessing.VideoProcessRequest.AdvancedOptions",
        description = "Opciones avanzadas para personalizar el comportamiento del algoritmo de recorte, " +
                "seguimiento y composición del video. Todos los campos son opcionales; si no se especifican, " +
                "el sistema aplicará valores por defecto optimizados para la plataforma seleccionada."
)
public record AdvancedOptions(

        @Schema(
                description = "Proporción de espacio adicional sobre la cabeza del sujeto principal. " +
                        "Permite ajustar el encuadre vertical. Rango permitido: 0.0 - 0.3.",
                example = "0.15",
                minimum = "0.0",
                maximum = "0.3"
        )
        @Min(value = 0, message = "El headroomRatio no puede ser negativo")
        @Max(value = 1, message = "Valor fuera de rango permitido")
        Double headroomRatio,

        @Schema(
                description = "Intensidad del suavizado aplicado al seguimiento de cámara. " +
                        "0.0 desactiva el suavizado, 1.0 aplica máxima estabilización.",
                example = "0.75",
                minimum = "0.0",
                maximum = "1.0"
        )
        @Min(value = 0, message = "El smoothingStrength no puede ser negativo")
        @Max(value = 1, message = "Valor fuera de rango permitido")
        Double smoothingStrength,

        @Schema(
                description = "Velocidad máxima permitida para el movimiento virtual de cámara. " +
                        "Valores más altos permiten recortes más dinámicos.",
                example = "25"
        )
        Integer maxCameraSpeed,

        @Schema(
                description = "Indica si se debe aplicar un filtro de nitidez al resultado final.",
                example = "true"
        )
        Boolean applySharpening,

        @Schema(
                description = "Indica si el algoritmo debe priorizar la composición basada en la regla de tercios.",
                example = "true"
        )
        Boolean useRuleOfThirds,

        @Schema(
                description = "Espacio adicional en píxeles entre el sujeto y los bordes del encuadre.",
                example = "20"
        )
        Integer edgePadding
) {}