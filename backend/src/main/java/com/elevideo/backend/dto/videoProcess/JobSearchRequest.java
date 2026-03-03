package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Schema(
        name = "VideoProcess.JobSearchRequest",
        description = "Parámetros opcionales para listar jobs activos con paginación."
)
public record JobSearchRequest(

        ProcessingMode processingMode,
        Platform platform,
        BackgroundMode backgroundMode,

        Integer page,
        Integer size,
        String sortBy,
        String sortDirection

) {

    public Pageable toPageable() {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 10;

        String sortField = sortBy != null && !sortBy.isBlank()
                ? sortBy
                : "createdAt";

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }
}