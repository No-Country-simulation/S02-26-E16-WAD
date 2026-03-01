package com.elevideo.backend.controller;

import com.elevideo.backend.documentation.videoProcessing.ProcessVideoEndpointDoc;
import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.videoProcess.*;
import com.elevideo.backend.service.VideoProcessingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videos/{videoId}")
@Tag(name = "05 - Procesamiento de Video",
        description = "Endpoints para el procesamiento y gestión de videos")
@SecurityRequirement(name = "bearerAuth")
public class VideoProcessingController {

    private final VideoProcessingService videoProcessingService;

    @ProcessVideoEndpointDoc
    @PostMapping("/process")
    public ResponseEntity<?> processVideo(@PathVariable Long videoId, @RequestBody @Valid VideoProcessRequest request) {

        VideoJobCreatedResponse response = videoProcessingService.processVideo(videoId, request);

        String message = switch (request.processingMode()) {
            case VERTICAL -> "Procesamiento vertical iniciado correctamente";
            case SHORT_AUTO -> "Generación de short automático iniciada";
            case SHORT_MANUAL -> "Generación de short manual iniciada";
        };

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResult.success(response, message));
    }


    @GetMapping("/rendition")
    public ResponseEntity<?> getVideosRendition(@PathVariable Long videoId, @ModelAttribute VideoRenditionSearchRequest request) {
        Page<VideoRenditionResponse> responses = videoProcessingService.getVideosRendition(videoId, request);
        return ResponseEntity
                .ok(ApiResult.success(responses, "Busqueda exitosa"));

    }
//    @GetMapping("/jobs")
//    public ResponseEntity<?> listJobs(
//            @PathVariable Long videoId) {
//
//        Object response =
//                videoProcessingService.listJobs(videoId);
//
//        return ResponseEntity.ok(
//                ApiResult.success(response, "Listado de jobs obtenido correctamente"));
//    }


    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJobStatus(
            @PathVariable Long videoId,
            @PathVariable String jobId) {

        JobResponse response =
                videoProcessingService.getJobStatus(videoId, jobId);

        return ResponseEntity.ok(
                ApiResult.success(response, "Estado del job obtenido correctamente"));
    }

    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<?> cancelJob(@PathVariable Long videoId, @PathVariable String jobId) {

        VideoJobCancelResponse response = videoProcessingService.cancelJob(videoId, jobId);

        return ResponseEntity.ok(
                ApiResult.success(response, "Job cancelado correctamente"));
    }


}
