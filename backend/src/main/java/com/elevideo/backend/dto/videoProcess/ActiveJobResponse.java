package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;

import java.time.LocalDateTime;

public record ActiveJobResponse(

        String jobId,
        String status,
        Integer progressPercent,
        String phase,

        ProcessingMode processingMode,
        Platform platform,
        BackgroundMode backgroundMode,

        LocalDateTime createdAt

) {}