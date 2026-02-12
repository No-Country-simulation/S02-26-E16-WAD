package com.elevideo.backend.documentation.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/auth/register.
 * Registra un nuevo usuario en el sistema.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Registrar nuevo usuario",
        description = """
            Crea una nueva cuenta de usuario en el sistema.
            
            **Flujo:**
            1. El usuario envía sus datos de registro
            2. El sistema valida que el email no esté registrado
            3. Crea el usuario con la contraseña encriptada
            4. Envía un email de verificación
            5. Devuelve los datos del usuario creado
            
            **Nota:** El usuario debe verificar su email antes de poder usar todas las funcionalidades.
            """
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "✅ Usuario registrado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Registro exitoso",
                                summary = "Respuesta exitosa de registro",
                                value = """
                        {
                          "success": true,
                          "message": "Registro completado con éxito",
                          "data": {
                            "id": "6fb1f53e-f1c6-4c87-9337-96062903b8f8",
                            "firstName": "Juan José",
                            "lastName": "Pérez Gómez",
                            "email": "juan.perez@example.com",
                            "emailVerified": false,
                            "createdAt": "2026-02-11T17:29:47.6700785"
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
                                summary = "Cuando los datos enviados no cumplen las validaciones",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:00",
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Error de validación en los campos",
                                      "fieldErrors": {
                                        "email": "debe ser una dirección de correo válida",
                                        "password": "debe tener al menos 8 caracteres",
                                        "firstName": "no debe estar vacío"
                                      },
                                      "path": "/api/v1/auth/register"
                                    }
                                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "❌ Email ya registrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Usuario ya existe",
                                summary = "El email ya está en uso",
                                value = """
                                    {
                                      "timestamp": "2026-02-10T16:30:15",
                                      "status": 409,
                                      "error": "USER_ALREADY_EXISTS",
                                      "message": "Usuario ya registrado",
                                      "details": [
                                        "El email ya está registrado"
                                      ],
                                      "path": "/api/v1/auth/register"
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
                                      "path": "/api/v1/auth/register"
                                    }
                                    """
                        )
                )
        )
})
public @interface RegisterEndpointDoc {
}