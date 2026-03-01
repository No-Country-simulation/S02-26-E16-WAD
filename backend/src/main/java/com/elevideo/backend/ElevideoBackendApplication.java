package com.elevideo.backend;

import com.elevideo.backend.config.JwtExpirationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtExpirationProperties.class)
public class ElevideoBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ElevideoBackendApplication.class, args);

	}
}