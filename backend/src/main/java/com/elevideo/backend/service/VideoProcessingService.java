package com.elevideo.backend.service;

import com.elevideo.backend.dto.videoProcess.*;
import org.springframework.data.domain.Page;

public interface VideoProcessingService {

    VideoJobCreatedResponse processVideo(Long videoId, VideoProcessRequest request);

    JobResponse getJobStatus(Long videoId, String jobId);

    VideoJobCancelResponse cancelJob(Long VideoId,String jobId);

    Page<VideoRenditionResponse> getVideosRendition(Long videoId, VideoRenditionSearchRequest request);

    VideoRenditionResponse getVideosRenditionById(Long videoId, Long renditionId);

    void deleteVideosRenditionById(Long videoId, Long renditionId);

    Page<ActiveJobResponse> listActiveJobs(
            Long videoId,
            JobSearchRequest request
    );
}
