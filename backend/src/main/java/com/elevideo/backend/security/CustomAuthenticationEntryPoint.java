package com.elevideo.backend.security;

import com.elevideo.backend.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manejador personalizado para errores de autenticación.
 * Se invoca cuando un usuario no autenticado intenta acceder a un recurso protegido.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        GlobalExceptionHandler.ErrorResponse error =
                GlobalExceptionHandler.ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("UNAUTHORIZED")
                        .message("No autenticado")
                        .details(List.of("Debes iniciar sesión para acceder a este recurso"))
                        .path(request.getRequestURI())
                        .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
