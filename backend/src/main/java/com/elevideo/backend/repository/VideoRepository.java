package com.elevideo.backend.repository;

import com.elevideo.backend.enums.VideoStatus;
import com.elevideo.backend.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("""
    SELECT v FROM Video v 
    WHERE v.project.id = :projectId
    AND (:searchTerm IS NULL OR v.title ILIKE :searchTerm)
    AND (:status IS NULL OR v.status = :status)
    """)
    Page<Video> findProjectVideos(
            @Param("projectId") Long projectId,
            @Param("searchTerm") String searchTerm,
            @Param("status") VideoStatus status,
            Pageable pageable
    );

}
