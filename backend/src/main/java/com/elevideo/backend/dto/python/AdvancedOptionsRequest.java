package com.elevideo.backend.dto.python;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdvancedOptionsRequest {

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double headroomRatio;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double smoothingStrength;

    @Min(1)
    @Max(100)
    private Integer maxCameraSpeed;

    private Boolean applySharpening;

    private Boolean useRuleOfThirds;

    @Min(0)
    @Max(500)
    private Integer edgePadding;
}
