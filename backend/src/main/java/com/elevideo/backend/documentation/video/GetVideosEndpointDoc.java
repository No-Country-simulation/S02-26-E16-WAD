package com.elevideo.backend.documentation.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * Documentación Swagger para GET /api/v1/projects/{projectId}/videos.
 * Lista los videos de un proyecto con filtros opcionales y paginación.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Listar videos del proyecto",
        description = """
            Obtiene todos los videos de un proyecto específico con opciones de búsqueda, filtrado y paginación.
            
            **Flujo:**
            1. Valida que el usuario esté autenticado
            2. Valida que el proyecto exista y pertenezca al usuario
            3. Aplica filtros opcionales (búsqueda por título, estado)
            4. Retorna los videos paginados y ordenados
            
            **Parámetros de búsqueda (todos opcionales):**
            - searchTerm: Busca en el título del video (case-insensitive)
            - status: Filtra por estado (UPLOADED, PROCESSING, READY, FAILED)
            - page: Número de página (default: 0)
            - size: Tamaño de página (default: 20)
            - sortBy: Campo de ordenamiento (default: createdAt)
            - sortDirection: Dirección ASC/DESC (default: DESC)
            
            **Nota:** Si no se proporcionan filtros, retorna todos los videos del proyecto.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "✅ Videos obtenidos exitosamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Lista de videos",
                                summary = "Respuesta con videos paginados",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Videos obtenidos correctamente",
                                      "data": {
                                        "content": [
                                          {
                                            "id": 1,
                                            "title": "Tutorial de React",
                                            "secureUrl": "https://res.cloudinary.com/.../video1.mp4",
                                            "format": "mp4",
                                            "durationInMillis": 125000,
                                            "sizeInBytes": 15728640,
                                            "width": 1920,
                                            "height": 1080,
                                            "status": "UPLOADED",
                                            "projectId": 5,
                                            "projectName": "Curso Frontend",
                                            "createdAt": "2024-02-16T10:30:00",
                                            "updatedAt": "2024-02-16T10:30:00"
                                          }
                                        ],
                                        "pageable": {
                                          "pageNumber": 0,
                                          "pageSize": 20
                                        },
                                        "totalElements": 8,
                                        "totalPages": 1,
                                        "first": true,
                                        "last": true
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
                                summary = "El usuario no tiene permiso para ver los videos de este proyecto",
                                value = """
                                    {
                                      "timestamp": "2024-02-16T10:30:00",
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "No tienes permiso para ver los videos de este proyecto",
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
                                      "message": "Error al obtener los videos",
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
public @interface GetVideosEndpointDoc {
}