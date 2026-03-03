package com.elevideo.backend.repository.spec;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.model.ProcessingJob;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class ProcessingJobSpecification {

    public static Specification<ProcessingJob> belongsToVideoAndUser(
            Long videoId,
            UUID userId
    ) {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("video").get("id"), videoId),
                        cb.equal(root.get("video")
                                .get("project")
                                .get("user")
                                .get("id"), userId)
                );
    }

    public static Specification<ProcessingJob> hasActiveStatus() {
        return (root, query, cb) ->
                root.get("status").in(
                        List.of(JobStatus.PENDING, JobStatus.PROCESSING)
                );
    }

    public static Specification<ProcessingJob> hasProcessingMode(
            ProcessingMode mode
    ) {
        return mode == null
                ? null
                : (root, query, cb) ->
                cb.equal(root.get("processingMode"), mode);
    }

    public static Specification<ProcessingJob> hasPlatform(
            Platform platform
    ) {
        return platform == null
                ? null
                : (root, query, cb) ->
                cb.equal(root.get("platform"), platform);
    }

    public static Specification<ProcessingJob> hasBackgroundMode(
            BackgroundMode mode
    ) {
        return mode == null
                ? null
                : (root, query, cb) ->
                cb.equal(root.get("backgroundMode"), mode);
    }
}
