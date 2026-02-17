package com.elevideo.backend.documentation.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/projects/{projectId}/videos.
 * Crea un video en un proyecto específico del usuario autenticado.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Crear video",
        description = """
            Sube un video a Cloudinary y lo registra en el proyecto especificado.
            
            **Flujo:**
            1. Valida que el usuario esté autenticado
            2. Valida que el proyecto exista y pertenezca al usuario
            3. Sube el video a Cloudinary
            4. Guarda la información del video en la base de datos
            5. Retorna los detalles del video creado
            
            **Nota:** El video debe ser un archivo válido (mp4, mov, avi, etc.)
            """,
        security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "✅ Video creado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Video creado",
                                summary = "Respuesta estándar cuando el video se crea correctamente",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Video creado correctamente",
                                      "data": {
                                        "id": 1,
                                        "title": "Tutorial de React",
                                        "secureUrl": "https://res.cloudinary.com/demo/video/upload/v123456/elevideo/video1.mp4",
                                        "durationInMillis": 125000,
                                        "width": 1920,
                                        "height": 1080,
                                        "status": "UPLOADED",
                                        "createdAt": "2024-02-16T10:30:00"
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
                                summary = "Cuando faltan campos requeridos o el archivo no es válido",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Error de validación en los campos",
                                      "fieldErrors": {
                                        "title": "no debe estar vacío",
                                        "video": "debe ser un archivo de video válido"
                                      },
                                      "path": "/api/v1/projects/5/videos"
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "❌ No autenticado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Sin autenticación",
                                summary = "Token no proporcionado o inválido",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Se requiere autenticación para acceder a este recurso",
                                      "path": "/api/v1/projects/5/videos"
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "❌ Sin permisos",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Proyecto no pertenece al usuario",
                                summary = "El usuario no tiene permiso para crear videos en este proyecto",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "No tienes permiso para crear videos en este proyecto",
                                      "details": [
                                        "El proyecto no pertenece al usuario autenticado"
                                      ],
                                      "path": "/api/v1/projects/5/videos"
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "❌ Proyecto no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Proyecto inexistente",
                                summary = "El proyecto especificado no existe",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "Proyecto no encontrado con id: 5",
                                      "path": "/api/v1/projects/5/videos"
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
                                summary = "Error inesperado al procesar la solicitud",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "Error al subir el video",
                                      "details": [
                                        "Ocurrió un error inesperado. Por favor, intenta más tarde"
                                      ],
                                      "path": "/api/v1/projects/5/videos"
                                    }
                                """
                        )
                )
        )
})
public @interface CreateVideoEndpointDoc {
}