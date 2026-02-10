package com.elevideo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.elevideo.backend.repository")
@EntityScan(basePackages = "com.elevideo.backend.model")
public class ElevideoBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ElevideoBackendApplication.class, args);

	}
}
