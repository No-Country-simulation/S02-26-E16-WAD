package com.elevideo.backend.repository;

import com.elevideo.backend.model.VideoRendition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRenditionRepository extends JpaRepository<VideoRendition, Long> {
}
