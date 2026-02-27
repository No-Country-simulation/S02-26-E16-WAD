package com.elevideo.backend.dto.python;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.enums.Quality;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoProcessingPythonRequest {

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

    @JsonProperty("advanced_options")
    private AdvancedOptionsRequest advancedOptions;
}
