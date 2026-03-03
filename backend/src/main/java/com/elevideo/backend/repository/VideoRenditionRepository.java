package com.elevideo.backend.repository;

import com.elevideo.backend.model.VideoRendition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface VideoRenditionRepository extends
        JpaRepository<VideoRendition, Long>,
        JpaSpecificationExecutor<VideoRendition> {


    Optional<VideoRendition> findByIdAndVideo_Project_User_Id(Long renditionId, UUID userId);
}
