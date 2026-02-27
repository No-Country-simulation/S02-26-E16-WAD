package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.python.VideoProcessingPythonRequest;
import com.elevideo.backend.dto.videoProcess.VideoVerticalCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PyhtonMapper {

    @Mapping(target = "cloudinaryInputUrl", source = "videoUrl")
    @Mapping(target = "platform", expression = "java(videoVertical.platform().getValue())")
    @Mapping(target = "quality", expression = "java(videoVertical.quality().getValue())")
    @Mapping(target = "backgroundMode", expression = "java(videoVertical.backgroundMode().getValue())")
    @Mapping(target = "processingMode", expression = "java(videoVertical.processingMode().getValue())")
    VideoProcessingPythonRequest toVideoProcessingPythonRequest(VideoVerticalCreateRequest videoVertical, String videoUrl);
}
