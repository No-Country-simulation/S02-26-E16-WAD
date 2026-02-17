package com.elevideo.backend.dto.video;

import com.elevideo.backend.enums.VideoStatus;

import java.time.LocalDateTime;

public record VideoResponse(
        Long id,
        String title,
        String secureUrl,
        Long durationInMillis,
        Integer width,
        Integer height,
        VideoStatus status,
        LocalDateTime createdAt
) {
}
