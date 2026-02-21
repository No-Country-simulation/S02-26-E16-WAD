//package com.elevideo.backend.dto.project;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public record ProjectResponse( Long id,
//                               String title,
//                               String description,
//                               LocalDateTime createdAt) {
//
//}

package com.elevideo.backend.dto.project;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
