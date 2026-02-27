package com.elevideo.backend.dto.videoProcess;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Respuesta del estado de un job de procesamiento.
 *
 * Se usa en dos contextos:
 *   1. Respuesta directa de Python (job en PENDING/PROCESSING) —
 *      Jackson deserializa snake_case con @JsonProperty.
 *   2. Construido desde ProcessingJob en BD (job COMPLETED/FAILED/CANCELLED) —
 *      ProcessingJobMapper lo construye directamente.
 */
public record VideoJobStatusResponse(

        @JsonProperty("job_id")
        String jobId,

        @JsonProperty("status")
        String status,

        @JsonProperty("progress")
        Integer progress,

        @JsonProperty("phase")
        String phase,

        @JsonProperty("output_url")
        String outputUrl,

        @JsonProperty("thumbnail_url")
        String thumbnailUrl,

        @JsonProperty("preview_url")
        String previewUrl,

        @JsonProperty("quality_score")
        Double qualityScore,

        @JsonProperty("segment_start")
        Double segmentStart,

        @JsonProperty("segment_duration")
        Integer segmentDuration,

        @JsonProperty("output_duration_seconds")
        Double outputDurationSeconds,

        @JsonProperty("error_detail")
        String errorDetail,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("completed_at")
        LocalDateTime completedAt
) {}