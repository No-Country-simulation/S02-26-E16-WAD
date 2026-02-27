package com.elevideo.backend.service.impl;

import com.elevideo.backend.dto.project.ProjectPageableRequest;
import com.elevideo.backend.dto.project.ProjectRequest;
import com.elevideo.backend.dto.project.ProjectResponse;
import com.elevideo.backend.exception.ProjectNotFoundException;
import com.elevideo.backend.mapper.ProjectMapper;
import com.elevideo.backend.model.Project;
import com.elevideo.backend.model.User;
import com.elevideo.backend.repository.ProjectRepository;
import com.elevideo.backend.repository.UserRepository;
import com.elevideo.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // =============================
    // CREATE
    // =============================
    @Override
    public ProjectResponse create(ProjectRequest request, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Project project = ProjectMapper.toEntity(request);
        project.setUser(user);

        Project saved = projectRepository.save(project);

        return ProjectMapper.toResponse(saved);
    }

    // =============================
    // GET ALL BY USER (DTO PAGE)
    // =============================
    @Override
    public Page<ProjectResponse> getProjectsByUser(UUID userId, ProjectPageableRequest pageable) {

        Page<Project> projectsPage = projectRepository.findByUser_Id(userId, pageable.toPageable());

        return ProjectMapper.toResponsePage(projectsPage);
    }

    // =============================
    // GET BY ID
    // =============================
    @Override
    public ProjectResponse getById(Long projectId, UUID userId) {

        Project project = projectRepository
                .findByIdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new ProjectNotFoundException("Proyecto no encontrado o no pertenece al usuario"));

        return ProjectMapper.toResponse(project);
    }

    // =============================
    // UPDATE
    // =============================
    @Override
    public ProjectResponse update(Long projectId, ProjectRequest request, UUID userId) {

        Project existing = projectRepository
                .findByIdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Proyecto no encontrado o no pertenece al usuario"));

        ProjectMapper.updateEntity(existing, request);

        Project updated = projectRepository.save(existing);

        return ProjectMapper.toResponse(updated);
    }

    // =============================
    // DELETE  Tarea: Crear alguna funcion que asegure borrar los archivos de la nube
    // =============================
    @Override
    public void delete(Long projectId, UUID userId) {

        Project existing = projectRepository
                .findByIdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Proyecto no encontrado o no pertenece al usuario"));

        projectRepository.delete(existing);
    }
}




