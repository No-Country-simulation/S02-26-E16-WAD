package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.*;

@Schema(
        name = "VideoRendition.VideoRenditionSearchRequest",
        description = "Parámetros para buscar rendiciones de un video específico. Todos los filtros son opcionales."
)
public record VideoRenditionSearchRequest(

        @Schema(description = "Modo de procesamiento aplicado.",
                example = "VERTICAL",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        ProcessingMode processingMode,

        @Schema(description = "Plataforma objetivo.",
                example = "TIKTOK",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Platform platform,

        @Schema(description = "Modo de fondo aplicado al video.",
                example = "BLURRED",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        BackgroundMode backgroundMode,

        @Schema(description = "Número de página (0-indexed).",
                example = "0",
                defaultValue = "0",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer page,

        @Schema(description = "Cantidad de elementos por página.",
                example = "20",
                defaultValue = "20",
                minimum = "1",
                maximum = "100",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer size,

        @Schema(description = "Campo para ordenar. Valores: createdAt, fileSizeBytes, durationInMillis.",
                example = "createdAt",
                defaultValue = "createdAt",
                allowableValues = {"createdAt", "fileSizeBytes", "durationInMillis"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String sortBy,

        @Schema(description = "Dirección del ordenamiento: ASC o DESC.",
                example = "DESC",
                defaultValue = "DESC",
                allowableValues = {"ASC", "DESC"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String sortDirection

) {

    public Pageable toPageable() {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;

        String sortField = (sortBy != null && !sortBy.isBlank())
                ? sortBy
                : "createdAt";

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }
}