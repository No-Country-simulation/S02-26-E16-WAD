package com.elevideo.backend.dto.python;

import com.elevideo.backend.dto.videoProcess.AdvancedOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoPythonRequest {

    @NotNull
    private String platform;

    @NotNull
    private String quality;

    @JsonProperty("background_mode")
    @NotNull
    private String backgroundMode;

    @JsonProperty("processing_mode")
    @NotNull
    private String processingMode;

    @JsonProperty("cloudinary_input_url")
    @NotBlank
    private String cloudinaryInputUrl;

    /**
     * Opciones para SHORT_MANUAL.
     * Solo se incluye en el JSON si no es null.
     */
    @JsonProperty("short_options")
    private ShortOptionsDto shortOptions;

    /**
     * Duración para SHORT_AUTO.
     * Solo se incluye en el JSON si no es null.
     */
    @JsonProperty("short_auto_duration")
    private Integer shortAutoDuration;

    /**
     * Opciones avanzadas (siempre opcional).
     */
    @JsonProperty("advanced_options")
    private AdvancedOptions advancedOptions;

    /**
     * DTO para opciones de short manual.
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ShortOptionsDto {

        @JsonProperty("start_time")
        @NotNull
        @Min(0)
        private Double startTime;

        @NotNull
        @Min(5)
        @Max(60)
        private Integer duration;
    }
}