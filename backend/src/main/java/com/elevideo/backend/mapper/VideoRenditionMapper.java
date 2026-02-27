package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import com.elevideo.backend.model.ProcessingJob;
import com.elevideo.backend.model.Video;
import com.elevideo.backend.model.VideoRendition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface VideoRenditionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processingMode", source = "request.processingMode")
    @Mapping(target = "createdAt", ignore = true)
    VideoRendition toVideoRendition(ProcessingJobWebhookRequest request);


}
