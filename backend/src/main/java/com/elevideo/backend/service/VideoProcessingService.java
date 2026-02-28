package com.elevideo.backend.service;

import com.elevideo.backend.aspect.LogExecution;
import com.elevideo.backend.dto.videoProcess.*;

public interface VideoProcessingService {

    VideoJobCreatedResponse processVideo(Long videoId, VideoProcessRequest request);

    JobResponse getJobStatus(Long videoId, String jobId);

//    VideoJobStatusResponse cancelJob(String jobId);
//
//    void deleteJob(String jobId);
//
//    Object listJobs();
}
