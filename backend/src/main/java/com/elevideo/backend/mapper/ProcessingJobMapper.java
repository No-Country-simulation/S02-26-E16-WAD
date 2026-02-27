package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.videoProcess.JobResponse;
import com.elevideo.backend.dto.videoProcess.VideoJobCreatedResponse;
import com.elevideo.backend.dto.videoProcess.VideoJobStatusResponse;
import com.elevideo.backend.dto.videoProcess.VideoVerticalCreateRequest;
import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.model.ProcessingJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", imports = {JobStatus.class})
public interface ProcessingJobMapper {

    @Mapping(target = "status", expression = "java(JobStatus.PENDING)")
    @Mapping(target = "processingMode", expression = "java(request.processingMode())")
    ProcessingJob toProcessiongJob(VideoVerticalCreateRequest request, VideoJobCreatedResponse response);

    void updateProcessingJob(ProcessingJobWebhookRequest request, @MappingTarget ProcessingJob processingJob);

    default LocalDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Mapping(target = "output.videoUrl", source = "outputUrl")
    @Mapping(target = "output.thumbnailUrl", source = "thumbnailUrl")
    @Mapping(target = "output.previewUrl", source = "previewUrl")
    JobResponse toJobResponse(VideoJobStatusResponse response);
}
