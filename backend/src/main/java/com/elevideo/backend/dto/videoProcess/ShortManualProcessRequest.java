package com.elevideo.backend.dto.videoProcess;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShortManualProcessRequest(
        @NotNull
        String platform,

        @NotNull
        String quality,

        @NotNull
        String backgroundMode,

        @NotNull
        @NotBlank
        String cloudinaryInputUrl,

        @NotNull
        ShortManualOptions shortOptions,

        AdvancedOptions advancedOptions
) {
    public record ShortManualOptions(
            @NotNull
            @Min(0)
            Double startTime,          // segundo de inicio del segmento

            @NotNull
            @Min(5) @Max(60)
            Integer duration           // duración en segundos
    ) {}
}
