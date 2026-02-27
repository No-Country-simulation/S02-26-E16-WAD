package com.elevideo.backend.dto.videoProcess;

public record AdvancedOptions(
        Double headroomRatio,      // 0.0 - 0.3
        Double smoothingStrength,  // 0.0 - 1.0
        Integer maxCameraSpeed,
        Boolean applySharpening,
        Boolean useRuleOfThirds,
        Integer edgePadding
) {}
