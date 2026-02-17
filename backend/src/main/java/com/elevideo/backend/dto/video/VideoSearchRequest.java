package com.elevideo.backend.dto.video;

import com.elevideo.backend.enums.VideoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Schema(
        name = "Video.VideoSearchRequest",
        description = "Parámetros para buscar y filtrar videos con paginación. Todos los campos son opcionales."
)
public record VideoSearchRequest(

        @Schema(
                description = "Término de búsqueda para filtrar videos por título. La búsqueda es case-insensitive y busca coincidencias parciales.",
                example = "tutorial",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String searchTerm,

        @Schema(
                description = "Estado del video para filtrar resultados. Valores permitidos: UPLOADED, PROCESSING, READY, FAILED.",
                example = "UPLOADED",
                allowableValues = {"UPLOADED", "PROCESSING", "READY", "FAILED"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        VideoStatus status,

        @Schema(
                description = "Número de página a obtener (0-indexed). La primera página es 0.",
                example = "0",
                defaultValue = "0",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Integer page,

        @Schema(
                description = "Cantidad de elementos por página.",
                example = "20",
                defaultValue = "20",
                minimum = "1",
                maximum = "100",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Integer size,

        @Schema(
                description = "Campo por el cual ordenar los resultados. Campos disponibles: createdAt, updatedAt, title, durationInMillis, sizeInBytes, status.",
                example = "createdAt",
                defaultValue = "createdAt",
                allowableValues = {"createdAt", "updatedAt", "title", "durationInMillis", "sizeInBytes", "status"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String sortBy,

        @Schema(
                description = "Dirección del ordenamiento. Valores permitidos: ASC (ascendente), DESC (descendente).",
                example = "DESC",
                defaultValue = "DESC",
                allowableValues = {"ASC", "DESC"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String sortDirection

) {

    /**
     * Convierte los parámetros de búsqueda a un objeto Pageable para Spring Data.
     * Aplica valores por defecto si no se proporcionan:
     * - page: 0
     * - size: 20
     * - sortBy: createdAt
     * - sortDirection: DESC
     *
     * @return Pageable configurado con los parámetros o valores por defecto
     */
    public Pageable toPageable() {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;

        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }
}

