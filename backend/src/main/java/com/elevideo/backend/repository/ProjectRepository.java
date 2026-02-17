package com.elevideo.backend.repository;

import com.elevideo.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserId(UUID userId);
    
    boolean existsByIdAndUserId(Long id, UUID userId);
}