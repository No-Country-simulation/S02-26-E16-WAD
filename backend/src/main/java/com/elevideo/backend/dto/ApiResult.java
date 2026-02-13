package com.elevideo.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estándar de respuesta de la API")
public record ApiResult<T>(

        @Schema(description = "Indica si la operación fue exitosa", example = "true")
        boolean success,

        @Schema(description = "Mensaje descriptivo de la respuesta", example = "Operación exitosa", maxLength = 500)
        String message,

        @Schema(description = "Datos retornados por la API (puede ser null si no hay datos)")
        T data
) {

  public static <T> ApiResult<T> success(T data, String message) {
    return new ApiResult<>(true, message, data);
  }

  public static <T> ApiResult<T> success(String message) {
    return new ApiResult<>(true, message, null);
  }
}
