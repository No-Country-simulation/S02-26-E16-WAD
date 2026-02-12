package com.elevideo.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "Auth.VerifyEmailRequest",
        description = "Solicitud para verificar la cuenta del usuario mediante un token enviado por correo electrónico.",
        requiredProperties = {"token"}
)
public record VerifyEmailReq(

        @Schema(
                description = "Token JWT de verificación de correo electrónico. Debe ser válido, no expirado y no utilizado previamente.",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @NotBlank(message = "El token de verificación es obligatorio")
        String token
) {
}
