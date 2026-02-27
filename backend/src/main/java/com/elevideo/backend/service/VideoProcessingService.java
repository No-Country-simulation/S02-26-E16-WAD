package com.elevideo.backend.service;

import com.elevideo.backend.dto.videoProcess.*;

public interface VideoProcessingService {

    VideoJobCreatedResponse processVertical(Long videoId, VideoVerticalCreateRequest request);

    VideoJobCreatedResponse processShortAuto(ShortAutoProcessRequest request);

    VideoJobCreatedResponse processShortManual(ShortManualProcessRequest request);

    JobResponse getJobStatus(Long videoId, String jobId);

    VideoJobStatusResponse cancelJob(String jobId);

    void deleteJob(String jobId);

    Object listJobs();
}
