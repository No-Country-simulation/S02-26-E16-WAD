package com.elevideo.backend.repository;

import com.elevideo.backend.model.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {

    Optional<ProcessingJob> findByJobId(String jobId);

    @Query("""
    SELECT p FROM ProcessingJob p
    WHERE p.jobId = :jobId
      AND p.video.id = :videoId
      AND p.video.project.user.id = :userId
""")
    Optional<ProcessingJob> findByJobIdAndVideoIdAndUserId(
            @Param("jobId")   String jobId,
            @Param("videoId") Long   videoId,
            @Param("userId")  UUID   userId
    );
}
