package com.elevideo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElevideoBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ElevideoBackendApplication.class, args);

	}
}