package com.elevideo.backend.documentation.auth;

import com.elevideo.backend.dto.auth.LoginRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/auth/login.
 * Autentica un usuario y devuelve un token JWT.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Iniciar sesión",
        description = """
            Autentica al usuario mediante email y contraseña.
            
            **Flujo:**
            1. El usuario envía sus credenciales
            2. El sistema valida email y contraseña
            3. Si son correctas, devuelve un token JWT
            4. El token debe incluirse en el header Authorization: Bearer {token}
            
            **Nota:** El token tiene una duración limitada configurada en el servidor.
            """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Autenticación exitosa",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Login exitoso",
                                summary = "Respuesta estándar de autenticación exitosa",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Inicio de sesión exitoso",
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiJ9..."
                                      }
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
                                name = "Campos inválidos",
                                summary = "Cuando faltan campos requeridos o tienen formato incorrecto",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:00",
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Error de validación en los campos",
                                      "fieldErrors": {
                                        "email": "debe ser una dirección de correo válida",
                                        "password": "no debe estar vacío"
                                      },
                                      "path": "/api/v1/auth/login"
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "❌ Credenciales incorrectas",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Credenciales inválidas",
                                summary = "Email o contraseña incorrectos",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:15",
                                      "status": 401,
                                      "error": "BAD_CREDENTIALS",
                                      "message": "Credenciales inválidas",
                                      "details": [
                                        "El correo electrónico o la contraseña son incorrectos"
                                      ],
                                      "path": "/api/v1/auth/login"
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
                                summary = "Error inesperado en el servidor",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:31:00",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "Error interno del servidor",
                                      "details": [
                                        "Ocurrió un error inesperado. Por favor, intenta más tarde"
                                      ],
                                      "path": "/api/v1/auth/login"
                                    }
                                    """
                        )
                )
        )
})
public @interface LoginEndpointDoc {
    
    /**
     * Esquema de respuesta exitosa de login.
     */
    class LoginSuccessResponse {
        public String token;
    }
}