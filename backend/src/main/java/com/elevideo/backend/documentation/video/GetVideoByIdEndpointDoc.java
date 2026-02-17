package com.elevideo.backend.documentation.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * Documentación Swagger para GET /api/v1/projects/{projectId}/videos/{videoId}.
 * Obtiene los detalles completos de un video específico.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Obtener video por ID",
        description = """
            Obtiene los detalles completos de un video específico.
            
            **Flujo:**
            1. Valida que el usuario esté autenticado
            2. Valida que el proyecto exista y pertenezca al usuario
            3. Valida que el video exista y pertenezca al proyecto
            4. Retorna los detalles completos del video
            
            **Nota:** Solo el propietario del proyecto puede ver los detalles del video.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Video obtenido exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Detalles del video",
                                summary = "Respuesta con información completa del video",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Video obtenido correctamente",
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
                                      "path": "/api/v1/projects/5/videos/1"
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
                                name = "Sin permisos para ver el video",
                                summary = "El usuario no tiene permiso para ver este video",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "No tienes permiso para ver este video",
                                      "details": [
                                        "El proyecto o video no pertenece al usuario autenticado"
                                      ],
                                      "path": "/api/v1/projects/5/videos/1"
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "❌ Recurso no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "Proyecto no encontrado",
                                        summary = "El proyecto especificado no existe",
                                        value = """
                                            {
                                              "timestamp": "2024-02-16T10:30:00",
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Proyecto no encontrado con id: 5",
                                              "path": "/api/v1/projects/5/videos/1"
                                            }
                                        """
                                ),
                                @ExampleObject(
                                        name = "Video no encontrado",
                                        summary = "El video especificado no existe",
                                        value = """
                                            {
                                              "timestamp": "2024-02-16T10:30:00",
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "Video no encontrado con id: 1",
                                              "path": "/api/v1/projects/5/videos/1"
                                            }
                                        """
                                )
                        }
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
                                      "message": "Error al obtener el video",
                                      "details": [
                                        "Ocurrió un error inesperado. Por favor, intenta más tarde"
                                      ],
                                      "path": "/api/v1/projects/5/videos/1"
                                    }
                                """
                        )
                )
        )
})
public @interface GetVideoByIdEndpointDoc {
}