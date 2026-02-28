package com.elevideo.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(
        name = "07 - System / Monitoring",
        description = "Endpoints de monitoreo del sistema utilizados para verificar disponibilidad, estado del servicio y diagnóstico básico."
)
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "OK",
                "service", "Elevideo Backend",
                "time", LocalDateTime.now().toString()
        );
    }
}
