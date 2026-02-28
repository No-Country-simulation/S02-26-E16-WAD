package com.elevideo.backend.controller;

import com.elevideo.backend.documentation.video.CreateVideoEndpointDoc;
import com.elevideo.backend.documentation.video.DeleteVideoEndpointDoc;
import com.elevideo.backend.documentation.video.GetVideoByIdEndpointDoc;
import com.elevideo.backend.documentation.video.GetVideosEndpointDoc;
import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.video.*;
import com.elevideo.backend.service.VideoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/videos")
@RequiredArgsConstructor
@Tag(name = "03 - Videos", description = "Endpoints para gestión de videos de proyectos")
public class VideoController {

    private final VideoService videoService;

    @CreateVideoEndpointDoc
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createVideo(@PathVariable Long projectId,@ModelAttribute @Valid CreateVideoRequest request) {
        VideoResponse response = videoService.createVideo(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response, "Video creado correctamente"));
    }

    @GetVideosEndpointDoc
    @GetMapping
    public ResponseEntity<?> getVideos(@PathVariable Long projectId, @ModelAttribute VideoSearchRequest searchParams){
        Page<VideoSummaryResponse> response = videoService.getVideos(projectId, searchParams);
        return ResponseEntity.ok(
                ApiResult.success(response, "Videos obtenidos correctamente")
        );
    }

    @GetVideoByIdEndpointDoc
    @GetMapping("/{videoId}")
    public ResponseEntity<?> getVideoById(@PathVariable Long projectId, @PathVariable Long videoId) {
        VideoSummaryResponse response = videoService.getVideoById(videoId);
        return ResponseEntity.ok(
                ApiResult.success(response, "Video obtenido correctamente")
        );
    }

    @DeleteVideoEndpointDoc
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}