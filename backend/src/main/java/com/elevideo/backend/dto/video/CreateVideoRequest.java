package com.elevideo.backend.dto.video;

import com.elevideo.backend.validators.VideoFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(
        name = "Video.CreateVideoRequest",
        description = "Datos necesarios para crear un nuevo video en un proyecto.",
        requiredProperties = {"title", "video"}
)
public record CreateVideoRequest(

        @Schema(
                description = "Título del video. Debe ser descriptivo y único dentro del proyecto.",
                example = "Tutorial de React - Introducción a Hooks",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "El título es requerido")
        String title,

        @Schema(
                description = "Archivo de video a subir. Formatos soportados: mp4, mov, avi, webm, mkv. Tamaño máximo: 200MB.",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El archivo de video es requerido")
        @VideoFile
        MultipartFile video

) {
}