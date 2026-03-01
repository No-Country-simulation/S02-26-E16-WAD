package com.elevideo.backend.repository.spec;


import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.model.VideoRendition;
import org.springframework.data.jpa.domain.Specification;

public class VideoRenditionSpecification {

    public static Specification<VideoRendition> belongsToVideo(Long videoId) {
        return (root, query, cb) ->
                cb.equal(root.get("video").get("id"), videoId);
    }

    public static Specification<VideoRendition> hasProcessingMode(ProcessingMode mode) {
        return (root, query, cb) ->
                mode == null ? null : cb.equal(root.get("processingMode"), mode);
    }

    public static Specification<VideoRendition> hasPlatform(Platform platform) {
        return (root, query, cb) ->
                platform == null ? null : cb.equal(root.get("platform"), platform);
    }

    public static Specification<VideoRendition> hasBackgroundMode(BackgroundMode mode) {
        return (root, query, cb) ->
                mode == null ? null : cb.equal(root.get("backgroundMode"), mode);
    }
}