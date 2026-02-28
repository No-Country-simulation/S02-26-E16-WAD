package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.enums.ProcessingMode;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record VideoJobCreatedResponse(
        @JsonProperty("job_id")
        UUID jobId,

        @JsonProperty("status")
        JobStatus status,

        @JsonProperty("message")
        String message,

        @JsonProperty("processing_mode")
        ProcessingMode processingMode

) {
}
