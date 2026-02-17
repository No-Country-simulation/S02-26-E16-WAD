package com.elevideo.backend.mapper;

import com.elevideo.backend.dto.cloudinary.CloudinaryUploadRes;
import com.elevideo.backend.dto.video.VideoResponse;
import com.elevideo.backend.dto.video.VideoSummaryResponse;
import com.elevideo.backend.enums.VideoStatus;
import com.elevideo.backend.model.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {VideoStatus.class})
public interface VideoMapper {

    @Mapping(target = "status", expression = "java(VideoStatus.UPLOADED)")
    Video toVideo (String title, CloudinaryUploadRes videoUploadRes);

    VideoResponse toVideoResponse (Video video);

    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectId", source = "project.id")
    VideoSummaryResponse toVideoSummaryResponse(Video video);

}
