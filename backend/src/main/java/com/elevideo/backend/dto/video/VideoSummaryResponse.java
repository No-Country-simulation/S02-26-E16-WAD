package com.elevideo.backend.dto.video;

import com.elevideo.backend.enums.VideoStatus;

import java.time.LocalDateTime;

public record VideoSummaryResponse(
        Long id,
        String title,
        String secureUrl,
        String format,
        Long durationInMillis,
        Long sizeInBytes,
        Integer width,
        Integer height,
        VideoStatus status,
        Long projectId,
        String projectName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
