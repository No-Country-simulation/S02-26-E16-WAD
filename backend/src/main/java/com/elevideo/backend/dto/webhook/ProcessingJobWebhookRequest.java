package com.elevideo.backend.dto.webhook;

import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.enums.ProcessingMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingJobWebhookRequest(

        @NotBlank
        @JsonProperty("job_id")
        String jobId,

        @NotBlank
        @JsonProperty("status")
        JobStatus status,

        @NotBlank
        @JsonProperty("processing_mode")
        ProcessingMode processingMode,

        @JsonProperty("elapsed_seconds")
        Double elapsedSeconds,

        @JsonProperty("phase")
        String phase,

        @NotNull
        @JsonProperty("completed_at")
        Instant completedAt,

        @JsonProperty("output_url")
        String outputUrl,

        @JsonProperty("thumbnail_url")
        String thumbnailUrl,

        @JsonProperty("preview_url")
        String previewUrl,

        @JsonProperty("quality_score")
        Double qualityScore,

        @JsonProperty("output_duration_seconds")
        Double outputDurationSeconds,

        @JsonProperty("segment_start")
        Double segmentStart,

        @JsonProperty("segment_duration")
        Integer segmentDuration,

        @JsonProperty("error_detail")
        String errorDetail
) {

        public boolean isCompleted() {
                return "completed".equalsIgnoreCase(status.name());
        }

        /**
         * Retorna true si el job falló.
         */
        public boolean isFailed() {
                return "failed".equalsIgnoreCase(status.name());
        }

        /**
         * Retorna true si el job fue cancelado.
         */
        public boolean isCancelled() {
                return "cancelled".equalsIgnoreCase(status.name());
        }

}
