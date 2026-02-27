package com.elevideo.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(

        @Schema(description = "Nombre(s) del usuario. Permitidos letras (incluye acentos), espacios, guiones y apóstrofes.", example = "Juan José")
        @NotBlank(message = "El nombre es requerido")
        @Pattern(regexp = "^[A-Za-zñáéíóúÁÉÍÓÚ]+(?: [A-Za-zñáéíóúÁÉÍÓÚ]+)*$", message = "El nombre contiene caracteres no permitidos")
        String firstName,

        @Schema(description = "Apellido(s) del usuario. Permitidos letras (incluye acentos), espacios, guiones y apóstrofes.", example = "Pérez Gómez")
        @NotBlank(message = "El apellido es requerido")
        @Pattern(regexp = "^[A-Za-zñáéíóúÁÉÍÓÚ]+(?: [A-Za-zñáéíóúÁÉÍÓÚ]+)*$", message = "El apellido contiene caracteres no permitidos")
        String lastName
) {
}
