package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.videoProcess.*;
import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.model.ProcessingJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", imports = {JobStatus.class, ProcessingMode.class})
public interface ProcessingJobMapper {

    @Mapping(target = "output.videoUrl", source = "outputUrl")
    @Mapping(target = "output.thumbnailUrl", source = "thumbnailUrl")
    @Mapping(target = "output.previewUrl", source = "previewUrl")
    JobResponse toJobResponse(VideoJobStatusResponse response);

    @Mapping(target = "jobId", source = "response.jobId")
    @Mapping(target = "status", source = "response.status")
    @Mapping(target = "processingMode", source = "request.processingMode")
    ProcessingJob toProcessingJob(VideoProcessRequest request, VideoJobCreatedResponse response);

    void updateProcessingJob(ProcessingJobWebhookRequest request, @MappingTarget ProcessingJob processingJob);




    default LocalDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }


}
