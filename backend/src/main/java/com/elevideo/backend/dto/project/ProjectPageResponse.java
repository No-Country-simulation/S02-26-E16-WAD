package com.elevideo.backend.dto.project;

import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    // Constructor compacto para aplicar valores por defecto
    public ProjectPageResponse {

        content = (content == null) ? List.of() : content;
        page = (page < 0) ? 0 : page;
        size = (size < 0) ? 0 : size;
        totalElements = (totalElements < 0) ? 0 : totalElements;
        totalPages = (totalPages < 0) ? 0 : totalPages;
        // last no necesita validación (boolean)
    }

    // Constructor vacío opcional para crear respuesta vacía fácilmente
    public static ProjectPageResponse empty() {
        return new ProjectPageResponse(
                List.of(),
                0,
                0,
                0,
                0,
                true
        );
    }
}
