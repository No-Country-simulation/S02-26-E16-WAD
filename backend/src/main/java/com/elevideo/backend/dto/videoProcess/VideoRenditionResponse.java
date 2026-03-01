package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;

import java.time.LocalDateTime;

public record VideoRenditionResponse(

        Long id,
        String outputUrl,
        String thumbnailUrl,
        String previewUrl,
        ProcessingMode processingMode,
        Platform platform,
        BackgroundMode backgroundMode,
        LocalDateTime createdAt
) {}
