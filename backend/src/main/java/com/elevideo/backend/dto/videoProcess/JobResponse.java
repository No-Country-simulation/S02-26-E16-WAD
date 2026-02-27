package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.JobStatus;

public record JobResponse(
        String jobId,
        String status,
        Integer progress,
        String phase,
        String errorDetail,
        Output output
) {
    public record Output(
            String videoUrl,
            String thumbnailUrl,
            String previewUrl
    ) {}
}


