package com.elevideo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configuración para el cliente HTTP de Resend.
 * RestClient es la recomendación actual de Spring (Spring 6.1+) sobre RestTemplate.
 */
@Configuration
public class EmailConfig {

    @Value("${resend.base-url:https://api.resend.com}")
    private String resendBaseUrl;

    /**
     * Bean de RestClient configurado para Resend API.
     * Incluye configuración base de headers y timeouts.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(resendBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}