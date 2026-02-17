package com.elevideo.backend.exception;

import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza el manejo de errores y devuelve respuestas consistentes con logging estructurado.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEPCIONES DE AUTENTICACIÓN ====================

    /**
     * Maneja credenciales incorrectas en el login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("🔐 Intento de login fallido en {}", request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("BAD_CREDENTIALS")
                .message("Credenciales inválidas")
                .details(List.of("El correo electrónico o la contraseña son incorrectos"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja excepciones de autenticación genéricas.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.error("🔒 Error de autenticación en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("AUTHENTICATION_ERROR")
                .message("Error de autenticación")
                .details(List.of("No se pudo autenticar al usuario"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja acceso denegado por falta de permisos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("⛔ Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("Acceso denegado")
                .details(List.of("No tienes permisos suficientes para realizar esta acción"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja JWT con firma inválida (posible manipulación o clave incorrecta).
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleJwtSignatureException(
            SignatureException ex,
            HttpServletRequest request) {

        log.warn("🛑 JWT con firma inválida en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("JWT_SIGNATURE_INVALID")
                .message("Token inválido o no confiable")
                .details(List.of("La firma del token no es válida o el token fue manipulado"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ==================== EXCEPCIONES DE NEGOCIO PERSONALIZADAS ====================

    /**
     * Maneja usuario ya existente en el registro.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("⚠️ Usuario ya existe: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("USER_ALREADY_EXISTS")
                .message("Usuario ya registrado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja usuario no encontrado.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        log.warn("🔍 Usuario no encontrado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("USER_NOT_FOUND")
                .message("Usuario no encontrado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja email ya verificado.
     */
    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyVerified(
            EmailAlreadyVerifiedException ex,
            HttpServletRequest request) {

        log.warn("✉️ Email ya verificado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("EMAIL_ALREADY_VERIFIED")
                .message("Email ya verificado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja token inválido o expirado.
     */
    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(TokenInvalidException ex, HttpServletRequest request) {
        log.warn("🎫 Token inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("INVALID_TOKEN")
                .message("Token inválido o expirado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja proyecto no encontrado.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(
            ProjectNotFoundException ex,
            HttpServletRequest request) {

        log.warn("📁 Proyecto no encontrado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("PROJECT_NOT_FOUND")
                .message("Proyecto no encontrado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja video no encontrado.
     */
    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVideoNotFound(
            VideoNotFoundException ex,
            HttpServletRequest request) {

        log.warn("🎬 Video no encontrado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("VIDEO_NOT_FOUND")
                .message("Video no encontrado")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja acceso prohibido por reglas de negocio.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request) {

        log.warn("⛔ Operación prohibida: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN_OPERATION")
                .message("No tienes permisos para realizar esta acción")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja errores al subir archivos a Cloudinary.
     */
    @ExceptionHandler(CloudinaryUploadException.class)
    public ResponseEntity<ErrorResponse> handleCloudinaryUploadException(
            CloudinaryUploadException ex,
            HttpServletRequest request) {

        log.error("☁️ Error al subir archivo a Cloudinary: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("CLOUDINARY_UPLOAD_ERROR")
                .message("Error al procesar el archivo en el servicio externo")
                .details(List.of("No se pudo completar la carga del archivo. Intenta nuevamente"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    // ==================== EXCEPCIONES DE VALIDACIÓN ====================

    /**
     * Maneja errores de validación de campos (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("📋 Errores de validación en {}", request.getRequestURI());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse error = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Error de validación en los campos")
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja argumentos ilegales.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("⚠️ Argumento ilegal en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("ILLEGAL_ARGUMENT")
                .message("Argumento inválido")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja estado ilegal.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error("⚠️ Estado ilegal en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("ILLEGAL_STATE")
                .message("Operación no permitida en el estado actual")
                .details(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ==================== EXCEPCIONES DE BASE DE DATOS ====================

    /**
     * Maneja violaciones de integridad de datos (duplicados, constraints, etc).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String rootMessage = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : ex.getMessage();

        log.warn("🗄️ Violación de integridad de datos en {}: {}", request.getRequestURI(), rootMessage);

        String userFriendlyMessage = "Conflicto con datos existentes";
        List<String> details = List.of("Los datos enviados entran en conflicto con registros existentes");

        // Detectar duplicados en PostgreSQL
        if (rootMessage != null && rootMessage.contains("Detail: Ya existe la llave")) {
            int startField = rootMessage.indexOf('(') + 1;
            int endField = rootMessage.indexOf(')', startField);
            int startValue = rootMessage.indexOf("=(", endField) + 2;
            int endValue = rootMessage.indexOf(").", startValue);

            if (startField > 0 && endField > startField && startValue > 1 && endValue > startValue) {
                String field = rootMessage.substring(startField, endField);
                String value = rootMessage.substring(startValue, endValue);
                userFriendlyMessage = String.format("El valor '%s' ya está registrado", value);
                details = List.of(String.format("El campo '%s' con valor '%s' ya existe en el sistema", field, value));
            }
        }
        // Detectar constraint violations
        else if (rootMessage != null && rootMessage.contains("violates")) {
            userFriendlyMessage = "Violación de restricción de base de datos";
            details = List.of("La operación viola una restricción de integridad");
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("DATA_INTEGRITY_VIOLATION")
                .message(userFriendlyMessage)
                .details(details)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ==================== EXCEPCIONES DE HTTP ====================

    /**
     * Maneja parámetros faltantes en la petición.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("📝 Parámetro faltante en {}: {}", request.getRequestURI(), ex.getParameterName());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MISSING_PARAMETER")
                .message("Falta un parámetro obligatorio")
                .details(List.of(String.format("El parámetro '%s' es obligatorio", ex.getParameterName())))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja tipos de parámetros incorrectos.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String paramName = ex.getName();
        String paramValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "desconocido";

        log.warn("🔢 Tipo de parámetro incorrecto en {}: {} (esperado: {})",
                request.getRequestURI(), paramName, expectedType);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("INVALID_PARAMETER_TYPE")
                .message("Tipo de parámetro inválido")
                .details(List.of(String.format(
                        "El parámetro '%s' recibió '%s' pero se esperaba tipo %s",
                        paramName, paramValue, expectedType)))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cuerpo de petición mal formado o ilegible.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("📄 Cuerpo de petición ilegible en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MESSAGE_NOT_READABLE")
                .message("Cuerpo de petición inválido")
                .details(List.of("El formato del JSON enviado es inválido o está mal formado"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja método HTTP no permitido.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String method = ex.getMethod();
        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "N/A";

        log.warn("🚫 Método HTTP no permitido en {}: {} (permitidos: {})",
                request.getRequestURI(), method, supportedMethods);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("METHOD_NOT_ALLOWED")
                .message("Método HTTP no permitido")
                .details(List.of(String.format(
                        "El método '%s' no está permitido. Métodos permitidos: %s",
                        method, supportedMethods)))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    /**
     * Maneja recurso o ruta no encontrada.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.warn("🔍 Recurso no encontrado: {}", request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("RESOURCE_NOT_FOUND")
                .message("Recurso no encontrado")
                .details(List.of(String.format("La ruta '%s' no existe", request.getRequestURI())))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja archivos que exceden el tamaño máximo permitido.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        log.warn("📦 Archivo excede el tamaño máximo permitido en {}",
                request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("PAYLOAD_TOO_LARGE")
                .message("El archivo excede el tamaño máximo permitido")
                .details(List.of("El tamaño máximo permitido para subida es 200MB"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // ==================== EXCEPCIÓN GENÉRICA ====================

    /**
     * Maneja cualquier excepción no capturada específicamente.
     * Este debe ser el último handler (catch-all).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("💥 Error no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("Error interno del servidor")
                .details(List.of("Ocurrió un error inesperado. Por favor, intenta más tarde"))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }




    // ==================== DTOs DE RESPUESTA ====================

    /**
     * DTO estándar de respuesta de error.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private List<String> details;
        private String path;
    }

    /**
     * DTO de respuesta de error de validación con errores por campo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> fieldErrors;
        private String path;
    }
}