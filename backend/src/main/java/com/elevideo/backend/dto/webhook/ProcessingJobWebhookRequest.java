package com.elevideo.backend.dto.webhook;

import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.enums.ProcessingMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(
        name = "Webhook.ProcessingJobCompletedRequest",
        description = "Payload enviado por el servicio de procesamiento cuando un job de video finaliza su ejecución. " +
                "Se ejecuta tanto en casos de éxito como de fallo, representando el estado final del procesamiento.",
        requiredProperties = {"job_id", "status", "processing_mode", "completed_at"}
)
public record ProcessingJobWebhookRequest(

        @Schema(
                description = "Identificador único del job de procesamiento generado por el servicio externo.",
                example = "a3f9c2e1-7b45-4f01-9c2d-2e7a3b91d001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "El jobId es requerido")
        @JsonProperty("job_id")
        String jobId,

        @Schema(
                description = "Estado final del job. Valores posibles: completed, failed, cancelled.",
                example = "completed",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El status es requerido")
        @JsonProperty("status")
        JobStatus status,

        @Schema(
                description = "Modo de procesamiento aplicado al video.",
                example = "vertical",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El processingMode es requerido")
        @JsonProperty("processing_mode")
        ProcessingMode processingMode,

        @Schema(
                description = "Tiempo total que tardó el procesamiento en segundos.",
                example = "125.4"
        )
        @JsonProperty("elapsed_seconds")
        Double elapsedSeconds,

        @Schema(
                description = "Última fase ejecutada antes de finalizar el procesamiento.",
                example = "rendering"
        )
        @JsonProperty("phase")
        String phase,

        @Schema(
                description = "Fecha y hora en la que el job finalizó su ejecución.",
                example = "2025-01-15T10:15:30Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La fecha de finalización es requerida")
        @JsonProperty("completed_at")
        Instant completedAt,

        @Schema(
                description = "URL pública del video procesado. Solo presente cuando el estado es completed.",
                example = "https://cdn.elevideo.com/output/video123.mp4"
        )
        @JsonProperty("output_url")
        String outputUrl,

        @Schema(
                description = "URL de la miniatura generada del video. Solo presente en procesamiento exitoso.",
                example = "https://cdn.elevideo.com/thumbnails/video123.jpg"
        )
        @JsonProperty("thumbnail_url")
        String thumbnailUrl,

        @Schema(
                description = "URL de vista previa del video procesado.",
                example = "https://cdn.elevideo.com/previews/video123.mp4"
        )
        @JsonProperty("preview_url")
        String previewUrl,

        @Schema(
                description = "Score de calidad calculado por el servicio de procesamiento.",
                example = "0.92"
        )
        @JsonProperty("quality_score")
        Double qualityScore,

        @Schema(
                description = "Duración final del video generado en segundos.",
                example = "58.3"
        )
        @JsonProperty("output_duration_seconds")
        Double outputDurationSeconds,

        @Schema(
                description = "Segundo de inicio del segmento utilizado (aplica para modos automáticos o segmentados).",
                example = "12.0"
        )
        @JsonProperty("segment_start")
        Double segmentStart,

        @Schema(
                description = "Duración del segmento generado en segundos.",
                example = "30"
        )
        @JsonProperty("segment_duration")
        Integer segmentDuration,

        @Schema(
                description = "Detalle del error cuando el procesamiento finaliza con estado failed o cancelled.",
                example = "FFmpeg rendering error: invalid codec configuration"
        )
        @JsonProperty("error_detail")
        String errorDetail
) {

        public boolean isCompleted() {
                return status == JobStatus.COMPLETED;
        }

        public boolean isFailed() {
                return status == JobStatus.FAILED;
        }

        public boolean isCancelled() {
                return status == JobStatus.CANCELLED;
        }

}