package com.elevideo.backend.documentation.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * Documentación Swagger para DELETE /api/v1/projects/{projectId}/videos/{id}.
 * Elimina un video del sistema y de Cloudinary.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Eliminar video",
        description = """
            Elimina permanentemente un video del sistema y de Cloudinary.
            
            **Flujo:**
            1. Valida que el usuario esté autenticado
            2. Valida que el proyecto exista y pertenezca al usuario
            3. Valida que el video exista y pertenezca al proyecto
            4. Elimina el video de Cloudinary
            5. Elimina el registro del video de la base de datos
            
            **Nota:** Esta acción es irreversible. Solo el propietario del proyecto puede eliminar videos.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "204",
                description = "✅ Video eliminado exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Eliminación exitosa",
                                summary = "El video fue eliminado correctamente (sin contenido en respuesta)",
                                value = ""
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
                                name = "Sin permisos para eliminar",
                                summary = "El usuario no tiene permiso para eliminar este video",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "No tienes permiso para eliminar este video",
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
                                summary = "Error inesperado al eliminar el video",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "Error al eliminar el video",
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
public @interface DeleteVideoEndpointDoc {
}