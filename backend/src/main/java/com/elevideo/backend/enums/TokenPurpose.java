package com.elevideo.backend.enums;

import com.elevideo.backend.config.JwtExpirationProperties;

public enum TokenPurpose {

    AUTHENTICATION,
    EMAIL_VERIFICATION,
    PASSWORD_RESET,
    PYTHON_SERVICE;

    public long resolveExpiration(JwtExpirationProperties props) {
        return switch (this) {
            case AUTHENTICATION -> props.getExpiration().getAuthentication();
            case EMAIL_VERIFICATION -> props.getExpiration().getEmailVerification();
            case PASSWORD_RESET -> props.getExpiration().getPasswordReset();
            case PYTHON_SERVICE -> props.getExpiration().getPythonService();
        };
    }

    public String resolveAudience() {
        return switch (this) {
            case PYTHON_SERVICE -> "python-service";
            default -> "public-client";
        };
    }
}