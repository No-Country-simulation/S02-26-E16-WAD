package com.elevideo.backend.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum JobStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    PROCESSING("processing");

    private final String value;

    JobStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static JobStatus fromValue(String value) {
        return Arrays.stream(JobStatus.values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valor inválido para ProcessingMode: " + value
                ));
    }
}
