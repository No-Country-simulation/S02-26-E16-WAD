package com.elevideo.backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRes(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String emailVerified,
        LocalDateTime createdAt
) {
    public void setFirstName(String firstName) {
    }
}
