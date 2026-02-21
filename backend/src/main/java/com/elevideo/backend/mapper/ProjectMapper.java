package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.project.ProjectRequest;
import com.elevideo.backend.dto.project.ProjectResponse;
import com.elevideo.backend.model.Project;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper manual entre Project Entity y sus DTOs.
 * Clase utilitaria estática (no necesita @Component).
 */
public final class ProjectMapper {

    private ProjectMapper() {
        // Evita instanciación
    }

    /**
     * Convierte ProjectRequest → Project Entity
     */
    public static Project toEntity(ProjectRequest request) {
        if (request == null) {
            return null;
        }

        Project project = new Project();
        project.setName(
                request.name() != null ? request.name().trim() : null
        );
        project.setDescription(request.description());

        return project;
    }

    /**
     * Actualiza una entidad existente con datos del request
     */
    public static void updateEntity(Project project, ProjectRequest request) {
        if (project == null || request == null) {
            return;
        }

        if (request.name() != null) {
            project.setName(request.name().trim());
        }

        project.setDescription(request.description());
    }

    /**
     * Convierte Project Entity → ProjectResponse DTO
     */
    public static ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    /**
     * Convierte lista de entidades → lista de DTOs
     */
    public static List<ProjectResponse> toResponseList(List<Project> projects) {
        if (projects == null) {
            return List.of();
        }

        return projects.stream()
                .filter(Objects::nonNull)
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte Page<Project> → Page<ProjectResponse>
     */
    public static Page<ProjectResponse> toResponsePage(Page<Project> page) {
        if (page == null) {
            return Page.empty();
        }

        return page.map(ProjectMapper::toResponse);
    }
}







//package com.elevideo.backend.mapper;
//
//import com.elevideo.backend.dto.project.ProjectRequest;
//import com.elevideo.backend.dto.project.ProjectResponse;
//import com.elevideo.backend.model.Project;
//
//public class ProjectMapper {
//
//    public static Project toEntity(ProjectRequest request) {
//        return Project.builder()
//                .name()
//                .describeConstable()
//                .wait();
//    }
//
//    public static ProjectResponse toResponse(Project project) {
//        return new ProjectResponse(
//                project.getId(),
//                project.getName(),
//                project.getDescription(),
//                project.getCreatedAt(),
//                project.getUpdatedAt()
//        );
//    }
//}
