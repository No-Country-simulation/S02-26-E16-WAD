package com.elevideo.backend.controller;

import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.project.ProjectPageableRequest;
import com.elevideo.backend.dto.project.ProjectRequest;
import com.elevideo.backend.dto.project.ProjectResponse;
import com.elevideo.backend.model.Project;
import com.elevideo.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "03 - Proyectos",
        description = "Endpoints para la gestión de proyectos")
public class ProjectController {

    private final ProjectService projectService;

    // ✅ Crear proyecto
    @PostMapping

    public ResponseEntity<?> createProject(
            @RequestBody ProjectRequest project,

            @AuthenticationPrincipal(expression = "id") UUID userId) {

        ProjectResponse created = projectService.create(project, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(created,"Proyecto creado con exito"));
    }

    // ✅ Listar proyectos del usuario (paginado)
    @GetMapping
    public ResponseEntity<?> getUserProjects(
            @AuthenticationPrincipal(expression = "id") UUID userId,
         @ModelAttribute ProjectPageableRequest pageable) {

        return ResponseEntity.ok(
                projectService.getProjectsByUser(userId, pageable));
    }

    // ✅ Obtener detalle
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "id") UUID userId) {

        return ResponseEntity.ok(
                projectService.getById(projectId, userId));
    }

    // ✅ Actualizar
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRequest project,
            @AuthenticationPrincipal(expression = "id") UUID userId) {

        return ResponseEntity.ok(
                projectService.update(projectId, project, userId));
    }

    // ✅ Eliminar
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "id") UUID userId) {

        projectService.delete(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
