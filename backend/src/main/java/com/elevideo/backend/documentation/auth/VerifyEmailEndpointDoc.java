package com.elevideo.backend.documentation.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/auth/verify-email.
 * Verifica el email del usuario mediante un token.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Verificar email",
        description = """
            Verifica el email del usuario usando el token enviado por correo.
            
            **Flujo:**
            1. El usuario recibe un email con un link de verificación
            2. El link contiene un token JWT
            3. El usuario hace click y el frontend envía el token a este endpoint
            4. El sistema valida el token y marca el email como verificado
            5. El token se agrega a la blacklist para prevenir reutilización
            
            **Nota:** El token tiene una validez limitada (generalmente 24 horas).
            """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Email verificado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Verificación exitosa",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Email verificado exitosamente",
                                      "data": {
                                        "email": "juan.perez@example.com",
                                        "emailVerified": true
                                      }
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "❌ Email ya verificado o validación fallida",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "Email ya verificado",
                                        summary = "El email ya fue verificado previamente",
                                        value = """
                                            {
                                              "timestamp": "2026-02-10T16:30:00",
                                              "status": 400,
                                              "error": "EMAIL_ALREADY_VERIFIED",
                                              "message": "Email ya verificado",
                                              "details": [
                                                "El email ya ha sido verificado previamente"
                                              ],
                                              "path": "/api/v1/auth/verify-email"
                                            }
                                            """
                                ),
                                @ExampleObject(
                                        name = "Validación fallida",
                                        summary = "Token no proporcionado o inválido",
                                        value = """
                                            {
                                              "timestamp": "2026-02-10T16:30:15",
                                              "status": 400,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Error de validación en los campos",
                                              "fieldErrors": {
                                                "token": "no debe estar vacío"
                                              },
                                              "path": "/api/v1/auth/verify-email"
                                            }
                                            """
                                )
                        }
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
                                      "timestamp": "2026-02-10T16:30:30",
                                      "status": 401,
                                      "error": "INVALID_TOKEN",
                                      "message": "Token inválido o expirado",
                                      "details": [
                                        "El token de verificación no es válido o ha expirado"
                                      ],
                                      "path": "/api/v1/auth/verify-email"
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
                                      "timestamp": "2026-02-10T16:30:45",
                                      "status": 404,
                                      "error": "USER_NOT_FOUND",
                                      "message": "Usuario no encontrado",
                                      "details": [
                                        "No se encontró el usuario con ID: 550e8400-e29b-41d4-a716-446655440000"
                                      ],
                                      "path": "/api/v1/auth/verify-email"
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
                                      "path": "/api/v1/auth/verify-email"
                                    }
                                    """
                        )
                )
        )
})
public @interface VerifyEmailEndpointDoc {
}