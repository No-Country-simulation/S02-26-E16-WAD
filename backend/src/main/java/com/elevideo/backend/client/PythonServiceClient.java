package com.elevideo.backend.client;

import com.elevideo.backend.exception.PythonServiceException;
import com.elevideo.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonServiceClient {

    private final JwtService serviceTokenService;

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    @Value("${python.service.api-key}")
    private String serviceApiKey;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    public <T> T post(String path, Object requestBody, Class<T> responseType, UUID userId) {

        try {
            return restClient.post()
                    .uri(pythonServiceUrl + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Service-Key", serviceApiKey)
                    .header("Authorization", "Bearer " + serviceTokenService.generateServiceToken(userId))
                    .body(requestBody)
                    .retrieve()
                    .body(responseType);

        } catch (HttpClientErrorException e) {
            handleClientError(e, path);
            return null;
        } catch (HttpServerErrorException e) {
            handleServerError(e, path);
            return null;
        } catch (ResourceAccessException e) {
            log.error("No se pudo conectar al microservicio Python | path={} | error={}", path, e.getMessage());
            throw new PythonServiceException(
                    "El servicio de procesamiento no está disponible. Intenta de nuevo en unos momentos."
            );
        }
    }

    /**
     * GET al microservicio Python.
     *
     * @param path         Path relativo, ej: "/api/video/status/abc-123"
     * @param responseType Clase esperada en la respuesta
     * @param userId       UUID del usuario autenticado en Spring Boot
     */
    public <T> T get(String path, Class<T> responseType, UUID userId) {
        log.debug("GET a Python service | path={} | userId={}", path, userId);

        try {
            return restClient.get()
                    .uri(pythonServiceUrl + path)
                    .header("X-Service-Key", serviceApiKey)
                    .header("Authorization", "Bearer " + serviceTokenService.generateServiceToken(userId))
                    .retrieve()
                    .body(responseType);

        } catch (HttpClientErrorException e) {
            handleClientError(e, path);
            return null; // unreachable
        } catch (HttpServerErrorException e) {
            handleServerError(e, path);
            return null; // unreachable
        } catch (ResourceAccessException e) {
            log.error("No se pudo conectar al microservicio Python | path={} | error={}", path, e.getMessage());
            throw new PythonServiceException(
                    "El servicio de procesamiento no está disponible. Intenta de nuevo en unos momentos."
            );
        }
    }

    /**
     * POST sin body (para cancelaciones, etc.).
     */
    public <T> T postEmpty(String path, Class<T> responseType, UUID userId) {
        log.debug("POST (sin body) a Python service | path={} | userId={}", path, userId);

        try {
            return restClient.post()
                    .uri(pythonServiceUrl + path)
                    .header("X-Service-Key", serviceApiKey)
                    .header("Authorization", "Bearer " + serviceTokenService.generateServiceToken(userId))
                    .retrieve()
                    .body(responseType);

        } catch (HttpClientErrorException e) {
            handleClientError(e, path);
            return null;
        } catch (HttpServerErrorException e) {
            handleServerError(e, path);
            return null;
        } catch (ResourceAccessException e) {
            log.error("No se pudo conectar al microservicio Python | path={} | error={}", path, e.getMessage());
            throw new PythonServiceException(
                    "El servicio de procesamiento no está disponible. Intenta de nuevo en unos momentos."
            );
        }
    }

    /**
     * DELETE al microservicio Python.
     */
    public void delete(String path, UUID userId) {
        log.debug("DELETE a Python service | path={} | userId={}", path, userId);

        try {
            restClient.delete()
                    .uri(pythonServiceUrl + path)
                    .header("X-Service-Key", serviceApiKey)
                    .header("Authorization", "Bearer " + serviceTokenService.generateServiceToken(userId))
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException e) {
            handleClientError(e, path);
        } catch (HttpServerErrorException e) {
            handleServerError(e, path);
        } catch (ResourceAccessException e) {
            log.error("No se pudo conectar al microservicio Python | path={} | error={}", path, e.getMessage());
            throw new PythonServiceException(
                    "El servicio de procesamiento no está disponible. Intenta de nuevo en unos momentos."
            );
        }
    }

    // ----------------------------------------------------------------
    // Manejo de errores
    // ----------------------------------------------------------------

    private void handleClientError(HttpClientErrorException e, String path) {
        log.warn("Error del cliente en Python service | path={} | status={} | body={}",
                path, e.getStatusCode(), e.getResponseBodyAsString());

        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new PythonServiceException("El recurso solicitado no existe.");
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
            log.error("Error de autenticación con el microservicio Python — "
                    + "verificar SERVICE_API_KEY y jwt.secret | path={}", path);
            throw new PythonServiceException(
                    "Error de configuración interna. Contacta al administrador."
            );
        }
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new PythonServiceException(
                    "La solicitud de procesamiento no es válida: " + e.getResponseBodyAsString()
            );
        }

        throw new PythonServiceException(
                "Error al comunicarse con el servicio de procesamiento."
        );
    }

    private void handleServerError(HttpServerErrorException e, String path) {
        log.error("Error del servidor en Python service | path={} | status={} | body={}",
                path, e.getStatusCode(), e.getResponseBodyAsString());

        throw new PythonServiceException(
                "El servicio de procesamiento falló internamente. Intenta de nuevo."
        );
    }
}