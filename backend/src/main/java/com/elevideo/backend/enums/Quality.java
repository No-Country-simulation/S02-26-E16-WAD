package com.elevideo.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Quality {
    LOW("fast"),
    NORMAL("normal"),
    HIGH("high");

    private final String value;

    Quality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Quality fromValue(String value) {
        return Arrays.stream(Quality.values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valor inválido para ProcessingMode: " + value
                ));
    }
}
