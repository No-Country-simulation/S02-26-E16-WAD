package com.elevideo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

  //@Value("${web.cors.allowed-origins}")
  //private String corsAllowedOrigins;

  //@Bean
  //public CorsConfigurationSource corsConfigurationSource() {
  //CorsConfiguration configuration = new CorsConfiguration();
   // configuration.setAllowedOriginPatterns(Arrays.stream(corsAllowedOrigins.split(",")).map(String::trim).toList());
   //configuration.setAllowedMethods(List.of("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
   // configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    //configuration.setAllowCredentials(true);
   // configuration.setMaxAge(3600L);

    //UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //source.registerCorsConfiguration("/**", configuration);
   // return source;

    @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Al usar "AllowedOriginPatterns" con "*" permites CUALQUIER dominio
    // Esto es ideal para repositorios públicos y simulaciones.
    configuration.setAllowedOriginPatterns(List.of("*"));
    
    configuration.setAllowedMethods(List.of("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
    
    // Esto es vital para que funcionen los tokens JWT
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

