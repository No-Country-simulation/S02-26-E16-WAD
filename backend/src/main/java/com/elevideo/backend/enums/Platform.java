package com.elevideo.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Platform {
    TIKTOK("tiktok"),
    INSTAGRAM("instagram"),
    YOUTUBE_SHORTS("youtube_shorts");

    private final String value;

    Platform(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Platform fromValue(String value) {
        return Arrays.stream(Platform.values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valor inválido para ProcessingMode: " + value
                ));
    }
}
