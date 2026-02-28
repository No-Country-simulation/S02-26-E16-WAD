package com.elevideo.backend.documentation.videoProcessing;

import com.elevideo.backend.dto.videoProcess.VideoProcessRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * Documentación Swagger para POST /api/v1/videos/{videoId}/process.
 * Procesa un video según el modo especificado (vertical, short automático o manual).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Procesar video (modo unificado)",
        description = """
            Procesa un video según el modo especificado en el campo 'processingMode'.
            
            **Flujo:**
            1. Valida que el usuario esté autenticado
            2. Valida que el video exista y pertenezca al usuario
            3. Valida los campos requeridos según el modo de procesamiento
            4. Envía la solicitud al servicio Python para procesamiento asíncrono
            5. Guarda el job en la base de datos
            6. Retorna el job_id para consultar el estado
            
            **Modos soportados:**
            - <b>VERTICAL:</b> Convierte el video completo a formato vertical 9:16
            - <b>SHORT_AUTO:</b> Genera un short seleccionando automáticamente el mejor segmento (requiere shortAutoDuration)
            - <b>SHORT_MANUAL:</b> Genera un short desde un segmento específico (requiere shortOptions)
            
            **Campos requeridos siempre:**
            - processingMode
            - platform
            - quality
            - backgroundMode
            
            **Campos condicionales:**
            - shortOptions: requerido si processingMode = SHORT_MANUAL
            - shortAutoDuration: requerido si processingMode = SHORT_AUTO
            - advancedOptions: siempre opcional
            
            **Nota:** El procesamiento es asíncrono. Usa el endpoint GET /api/v1/videos/{videoId}/jobs/{jobId} 
            para consultar el estado del procesamiento.
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
                description = "Configuración del procesamiento según el modo seleccionado",
                required = true,
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = VideoProcessRequest.class),
                        examples = {
                                @ExampleObject(
                                        name = "Vertical",
                                        summary = "Procesar video completo en vertical",
                                        description = "Convierte el video completo a formato vertical 9:16 con recorte inteligente",
                                        value = """
                                            {
                                              "processingMode": "VERTICAL",
                                              "platform": "TIKTOK",
                                              "quality": "NORMAL",
                                              "backgroundMode": "SMART_CROP",
                                              "advancedOptions": {
                                                "headroomRatio": 0.2,
                                                "smoothingStrength": 0.9
                                              }
                                            }
                                            """
                                ),
                                @ExampleObject(
                                        name = "Short Auto",
                                        summary = "Generar short automático de 30 segundos",
                                        description = "Selecciona automáticamente el mejor segmento del video y lo convierte a vertical",
                                        value = """
                                            {
                                              "processingMode": "SHORT_AUTO",
                                              "platform": "INSTAGRAM",
                                              "quality": "HIGH",
                                              "backgroundMode": "BLURRED",
                                              "shortAutoDuration": 30
                                            }
                                            """
                                ),
                                @ExampleObject(
                                        name = "Short Manual",
                                        summary = "Generar short desde el minuto 2 por 15 segundos",
                                        description = "Extrae un segmento específico del video y lo convierte a formato vertical",
                                        value = """
                                            {
                                              "processingMode": "SHORT_MANUAL",
                                              "platform": "YOUTUBE_SHORTS",
                                              "quality": "NORMAL",
                                              "backgroundMode": "BLACK",
                                              "shortOptions": {
                                                "startTime": 120.0,
                                                "duration": 15
                                              }
                                            }
                                            """
                                )
                        }
                )
        )
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "202",
                description = "✅ Procesamiento iniciado correctamente",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Job creado",
                                summary = "Respuesta estándar cuando el procesamiento inicia correctamente",
                                value = """
                                    {
                                      "success": true,
                                      "message": "Procesamiento vertical iniciado correctamente",
                                      "data": {
                                        "jobId": "18e48a1a-3ba5-4962-a4fd-867ddb4bd6e1",
                                        "status": "pending",
                                        "message": "El video está en cola para procesarse"
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
                        examples = {
                                @ExampleObject(
                                        name = "Campos faltantes",
                                        summary = "Cuando faltan campos requeridos",
                                        value = """
                                            {
                                              "timestamp": "2024-02-20T10:30:00",
                                              "status": 400,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Error de validación en los campos",
                                              "fieldErrors": {
                                                "platform": "no debe ser nulo",
                                                "quality": "no debe ser nulo",
                                                "backgroundMode": "no debe ser nulo"
                                              },
                                              "path": "/api/v1/videos/123/process"
                                            }
                                        """
                                ),
                                @ExampleObject(
                                        name = "Short Manual sin opciones",
                                        summary = "Cuando se selecciona SHORT_MANUAL pero falta shortOptions",
                                        value = """
                                            {
                                              "timestamp": "2024-02-20T10:30:00",
                                              "status": 400,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Error de validación en los campos",
                                              "fieldErrors": {
                                                "shortOptions": "El campo 'shortOptions' es requerido cuando processingMode = SHORT_MANUAL"
                                              },
                                              "path": "/api/v1/videos/123/process"
                                            }
                                        """
                                ),
                                @ExampleObject(
                                        name = "Short Auto sin duración",
                                        summary = "Cuando se selecciona SHORT_AUTO pero falta shortAutoDuration",
                                        value = """
                                            {
                                              "timestamp": "2024-02-20T10:30:00",
                                              "status": 400,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Error de validación en los campos",
                                              "fieldErrors": {
                                                "shortAutoDuration": "El campo 'shortAutoDuration' es requerido cuando processingMode = SHORT_AUTO"
                                              },
                                              "path": "/api/v1/videos/123/process"
                                            }
                                        """
                                ),
                                @ExampleObject(
                                        name = "Valores fuera de rango",
                                        summary = "Cuando los valores no cumplen las restricciones",
                                        value = """
                                            {
                                              "timestamp": "2024-02-20T10:30:00",
                                              "status": 400,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Error de validación en los campos",
                                              "fieldErrors": {
                                                "shortAutoDuration": "La duración debe estar entre 5 y 60 segundos",
                                                "shortOptions.duration": "La duración debe estar entre 5 y 60 segundos"
                                              },
                                              "path": "/api/v1/videos/123/process"
                                            }
                                        """
                                )
                        }
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
                                      "timestamp": "2024-02-20T10:30:00",
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Se requiere autenticación para acceder a este recurso",
                                      "path": "/api/v1/videos/123/process"
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
                                name = "Video no pertenece al usuario",
                                summary = "El usuario no tiene permiso para procesar este video",
                                value = """
                                    {
                                      "timestamp": "2024-02-20T10:30:00",
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "No tienes permiso para procesar este video",
                                      "details": [
                                        "El video no pertenece al usuario autenticado"
                                      ],
                                      "path": "/api/v1/videos/123/process"
                                    }
                                """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "❌ Video no encontrado",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Video inexistente",
                                summary = "El video especificado no existe",
                                value = """
                                    {
                                      "timestamp": "2024-02-20T10:30:00",
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "Video no encontrado con id: 123",
                                      "path": "/api/v1/videos/123/process"
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
                                summary = "Error inesperado al comunicarse con el servicio de procesamiento",
                                value = """
                                    {
                                      "timestamp": "2024-02-20T10:30:00",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "Error al iniciar el procesamiento del video",
                                      "details": [
                                        "No se pudo conectar con el servicio Python",
                                        "Por favor, intenta más tarde"
                                      ],
                                      "path": "/api/v1/videos/123/process"
                                    }
                                """
                        )
                )
        )
})
public @interface ProcessVideoEndpointDoc {
}