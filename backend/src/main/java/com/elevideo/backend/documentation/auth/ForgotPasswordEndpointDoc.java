package com.elevideo.backend.documentation.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/auth/forgot-password.
 * Inicia el proceso de recuperación de contraseña.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Solicitar recuperación de contraseña",
        description = """
            Inicia el proceso de recuperación de contraseña enviando un email con instrucciones.
            
            **Flujo:**
            1. El usuario envía su email
            2. Si el email existe, el sistema genera un token de reseteo
            3. Se envía un email con un link para resetear la contraseña
            4. El link contiene el token que será usado en /reset-password
            
            **Nota de seguridad:** Por razones de seguridad, siempre devuelve 200 OK incluso si
            el email no existe, para no revelar qué emails están registrados en el sistema.
            """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Email enviado (si la cuenta existe)",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Solicitud procesada correctamente",
                                summary = "Respuesta estándar para recuperación de contraseña",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Se ha enviado un correo para restablecer tu contraseña",
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
                                name = "Email inválido",
                                summary = "Email no proporcionado o formato incorrecto",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:00",
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Error de validación en los campos",
                                      "fieldErrors": {
                                        "email": "debe ser una dirección de correo válida"
                                      },
                                      "path": "/api/v1/auth/forgot-password"
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
                                      "path": "/api/v1/auth/forgot-password"
                                    }
                                    """
                        )
                )
        )
})
public @interface ForgotPasswordEndpointDoc {
}