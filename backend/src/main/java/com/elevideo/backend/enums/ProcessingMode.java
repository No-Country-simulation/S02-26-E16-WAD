package com.elevideo.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ProcessingMode {

    VERTICAL("vertical"),
    SHORT_AUTO("short_auto"),
    SHORT_MANUAL("short_manual");

    private final String value;

    ProcessingMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProcessingMode fromValue(String value) {
        return Arrays.stream(ProcessingMode.values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valor inválido para ProcessingMode: " + value
                ));
    }
}