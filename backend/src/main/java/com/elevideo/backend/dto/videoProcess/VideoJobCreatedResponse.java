package com.elevideo.backend.dto.videoProcess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record VideoJobCreatedResponse(
        @JsonProperty("job_id")
        UUID jobId,

        @JsonProperty("status")
        String status,

        @JsonProperty("message")
        String message,

        @JsonProperty("processing_mode")
        String processingMode

) {
}
