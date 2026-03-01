package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.python.VideoPythonRequest;
import com.elevideo.backend.dto.videoProcess.VideoProcessRequest;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.model.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {ProcessingMode.class})
public interface VideoProcessingMapper {

    /**
     * Convierte VideoProcessRequest + Video a VideoPythonRequest.
     *
     * Este método único reemplaza:
     * - toVideoProcessingPythonRequest (vertical)
     * - toShortManualProcessingPythonRequest (short manual)
     * - toShortAutpProcessingPythonRequest (short auto)
     */
    @Mapping(target = "cloudinaryInputUrl", source = "video.secureUrl")
    @Mapping(target = "platform", expression = "java(request.platform().getValue())")
    @Mapping(target = "quality", expression = "java(request.quality().getValue())")
    @Mapping(target = "backgroundMode", expression = "java(request.backgroundMode().getValue())")
    @Mapping(target = "processingMode", expression = "java(request.processingMode().getValue())")
    @Mapping(target = "shortOptions", source = "request.shortOptions")
    @Mapping(target = "shortAutoDuration", source = "request.shortAutoDuration")
    @Mapping(target = "advancedOptions", source = "request.advancedOptions")
    VideoPythonRequest toVideoPythonRequest(VideoProcessRequest request, Video video);

    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "duration", source = "duration")
    VideoPythonRequest.ShortOptionsDto toShortOptionsDto(
            VideoProcessRequest.ShortManualOptions options
    );
}
