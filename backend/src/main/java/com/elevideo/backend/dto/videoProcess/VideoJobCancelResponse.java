package com.elevideo.backend.dto.videoProcess;

import com.elevideo.backend.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VideoJobCancelResponse(
        @JsonProperty("job_id")
                String jobId,

        @JsonProperty("message")
        String message,

        @JsonProperty("previous_status")
        JobStatus previousStatus
) {}


