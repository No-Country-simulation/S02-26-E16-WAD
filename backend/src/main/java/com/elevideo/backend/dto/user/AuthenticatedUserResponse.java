package com.elevideo.backend.dto.user;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
