package com.elevideo.backend.repository;

import com.elevideo.backend.model.VideoRendition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VideoRenditionRepository extends
        JpaRepository<VideoRendition, Long>,
        JpaSpecificationExecutor<VideoRendition> {


}
