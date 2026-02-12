package com.elevideo.backend.documentation.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/auth/reset-password.
 * Restablece la contraseña del usuario usando un token válido.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Restablecer contraseña",
        description = """
            Restablece la contraseña del usuario usando el token recibido por email.
            
            **Flujo:**
            1. El usuario recibe un email de recuperación con un token
            2. El usuario hace click en el link y es redirigido a una página de reseteo
            3. El frontend envía el token y la nueva contraseña a este endpoint
            4. El sistema valida el token y actualiza la contraseña
            5. El token se agrega a la blacklist para prevenir reutilización
            
            **Nota:** El token tiene una validez limitada (generalmente 1 hora).
            """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Contraseña restablecida exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Reseteo exitoso",
                                summary = "Respuesta estándar de éxito",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Contraseña restablecida correctamente",
                                      "data": null
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "❌ Error de validación",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Validación fallida",
                                summary = "Token o contraseña no proporcionados o inválidos",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:00",
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Error de validación en los campos",
                                      "fieldErrors": {
                                        "token": "no debe estar vacío",
                                        "newPassword": "debe tener al menos 8 caracteres"
                                      },
                                      "path": "/api/v1/auth/reset-password"
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "❌ Token inválido o expirado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Token inválido",
                                summary = "El token no es válido o ya expiró",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:15",
                                      "status": 401,
                                      "error": "INVALID_TOKEN",
                                      "message": "Token inválido o expirado",
                                      "details": [
                                        "El token de reseteo no es válido o ha expirado"
                                      ],
                                      "path": "/api/v1/auth/reset-password"
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "❌ Usuario no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Usuario no encontrado",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:30",
                                      "status": 404,
                                      "error": "USER_NOT_FOUND",
                                      "message": "Usuario no encontrado",
                                      "details": [
                                        "No se encontró el usuario con ID: 550e8400-e29b-41d4-a716-446655440000"
                                      ],
                                      "path": "/api/v1/auth/reset-password"
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "❌ Error interno del servidor",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Error interno",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:31:00",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "Error interno del servidor",
                                      "details": [
                                        "Ocurrió un error inesperado. Por favor, intenta más tarde"
                                      ],
                                      "path": "/api/v1/auth/reset-password"
                                    }
                                    """
                        )
                )
        )
})
public @interface ResetPasswordEndpointDoc {
}