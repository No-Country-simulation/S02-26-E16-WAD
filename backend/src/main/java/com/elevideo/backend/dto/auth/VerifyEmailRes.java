package com.elevideo.backend.dto.auth;

public record VerifyEmailRes(
        String email,
        boolean emailVerified
) {
}
