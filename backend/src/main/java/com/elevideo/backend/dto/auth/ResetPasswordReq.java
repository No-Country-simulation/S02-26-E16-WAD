package com.elevideo.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "Auth.ResetPasswordRequest",
        description = "Petición para restablecer la contraseña de un usuario",
        requiredProperties = {"token", "newPassword"})
public record ResetPasswordReq(

        @Schema(description = "Token JWT enviado al correo electronico", example = "eyJhbGciOiJI...")
        @NotBlank(message = "token es requerido")
        String token,

        @Schema(description = "Nueva contraseña del usuario.", example = "Nu3v0P@ssw0rd!")
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, max = 64, message = "newPassword debe tener entre 8 y 64 caracteres")
        @Pattern(regexp = "^(?=.*[A-ZÑ])(?=.*[a-zñ])(?=.*\\d)(?=.*[-@#$%^&*.,()_+{}|;:'\"<>/!¡¿?])[A-ZÑa-zñ\\d-@#$%^&*.,()_+{}|;:'\"<>/!¡¿?]{6,}$",
                message = "La contraseña debe contener al menos una letra mayuscula, una letra minuscula, un numero, y un caracter especial.")
        String newPassword
) {
}
