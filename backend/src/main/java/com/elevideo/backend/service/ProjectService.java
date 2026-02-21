package com.elevideo.backend.service;

import com.elevideo.backend.dto.project.ProjectPageableRequest;
import com.elevideo.backend.dto.project.ProjectRequest;
import com.elevideo.backend.dto.project.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio de gestión de proyectos.
 * Define las operaciones disponibles para la capa de negocio.
 */
public interface ProjectService {

    /**
     * Crear un nuevo proyecto para un usuario.
     */
    ProjectResponse create(ProjectRequest request, UUID userId);

    /**
     * Obtener proyectos del usuario autenticado con paginación.
     */
    Page<ProjectResponse> getProjectsByUser(UUID userId, ProjectPageableRequest pageable);

    /**
     * Obtener un proyecto específico por ID validando que pertenezca al usuario.
     */
    ProjectResponse getById(Long projectId, UUID userId);

    /**
     * Actualizar un proyecto existente.
     */
    ProjectResponse update(Long projectId, ProjectRequest request, UUID userId);

    /**
     * Eliminar un proyecto.
     */
    void delete(Long projectId, UUID userId);
}




