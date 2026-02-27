package com.elevideo.backend.dto.videoProcess;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShortAutoProcessRequest(
        @NotNull
        String platform,

        @NotNull
        String quality,

        @NotNull
        String backgroundMode,

        @NotNull
        @NotBlank
        String cloudinaryInputUrl,

        String processingMode,     // siempre "short_auto"

        @NotNull
        @Min(5) @Max(60)
        Integer shortAutoDuration, // duración del short en segundos

        @JsonProperty("advanced_options")
        AdvancedOptions advancedOptions
) {
}
