package com.elevideo.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record JwtDataDto(
        @Schema(description = "Identificador único del usuario en formato UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Email del usuario", example = "juan.perez_92@example.com")
        String email,

        @Schema(description = "Nombre del usuario", example = "Juan Jose")
        String firstName,

        @Schema(description = "Apellido del usuario", example = "Perez Gomez")
        String lastName

) {
}
