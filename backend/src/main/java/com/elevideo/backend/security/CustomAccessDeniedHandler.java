package com.elevideo.backend.security;

import com.elevideo.backend.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manejador personalizado para errores de acceso denegado.
 * Se invoca cuando un usuario autenticado intenta acceder a un recurso para el cual no tiene permisos.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        GlobalExceptionHandler.ErrorResponse error =
                GlobalExceptionHandler.ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("FORBIDDEN")
                        .message("Acceso denegado")
                        .details(List.of("No tienes permisos suficientes para este recurso"))
                        .path(request.getRequestURI())
                        .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}