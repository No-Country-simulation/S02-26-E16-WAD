package com.elevideo.backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;

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
}
