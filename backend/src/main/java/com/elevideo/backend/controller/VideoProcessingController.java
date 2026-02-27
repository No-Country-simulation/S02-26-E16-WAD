package com.elevideo.backend.controller;

import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.videoProcess.JobResponse;
import com.elevideo.backend.dto.videoProcess.VideoJobCreatedResponse;
import com.elevideo.backend.dto.videoProcess.VideoJobStatusResponse;
import com.elevideo.backend.dto.videoProcess.VideoVerticalCreateRequest;
import com.elevideo.backend.enums.JobStatus;
import com.elevideo.backend.service.VideoProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videos/{videoId}")
@Tag(name = "02 - Procesamiento de Video",
        description = "Procesamiento y gestión de versiones derivadas")
@SecurityRequirement(name = "bearerAuth")
public class VideoProcessingController {

    private final VideoProcessingService videoProcessingService;

    @PostMapping("/vertical")
    public ResponseEntity<ApiResult<VideoJobCreatedResponse>> processVertical(
            @PathVariable Long videoId,
            @RequestBody @Valid VideoVerticalCreateRequest request) {

        VideoJobCreatedResponse response =
                videoProcessingService.processVertical(videoId, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResult.success(response, "Proceso vertical iniciado correctamente"));
    }

//    @PostMapping("/short/auto")
//    public ResponseEntity<ApiResult<VideoJobCreatedResponse>> processShortAuto(
//            @PathVariable Long videoId,
//            @RequestBody @Valid ShortAutoProcessRequest request) {
//
//        VideoJobCreatedResponse response =
//                videoProcessingService.processShortAuto(videoId, request);
//
//        return ResponseEntity.status(HttpStatus.ACCEPTED)
//                .body(ApiResult.success(response, "Short automático iniciado correctamente"));
//    }
//
//    @PostMapping("/short/manual")
//    public ResponseEntity<ApiResult<VideoJobCreatedResponse>> processShortManual(
//            @PathVariable Long videoId,
//            @RequestBody @Valid ShortManualProcessRequest request) {
//
//        VideoJobCreatedResponse response =
//                videoProcessingService.processShortManual(videoId, request);
//
//        return ResponseEntity.status(HttpStatus.ACCEPTED)
//                .body(ApiResult.success(response, "Short manual iniciado correctamente"));
//    }
//
//    @GetMapping("/jobs")
//    public ResponseEntity<ApiResult<Object>> listJobs(
//            @PathVariable Long videoId) {
//
//        Object response =
//                videoProcessingService.listJobs(videoId);
//
//        return ResponseEntity.ok(
//                ApiResult.success(response, "Listado de jobs obtenido correctamente"));
//    }
//

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResult<JobResponse>> getJobStatus(
            @PathVariable Long videoId,
            @PathVariable String jobId) {

        JobResponse response =
                videoProcessingService.getJobStatus(videoId, jobId);

        return ResponseEntity.ok(
                ApiResult.success(response, "Estado del job obtenido correctamente"));
    }
//
//    @PostMapping("/jobs/{jobId}/cancel")
//    public ResponseEntity<ApiResult<VideoJobStatusResponse>> cancelJob(
//            @PathVariable Long videoId,
//            @PathVariable String jobId) {
//
//        VideoJobStatusResponse response =
//                videoProcessingService.cancelJob(videoId, jobId);
//
//        return ResponseEntity.ok(
//                ApiResult.success(response, "Job cancelado correctamente"));
//    }
//
//    @DeleteMapping("/jobs/{jobId}")
//    public ResponseEntity<Void> deleteJob(
//            @PathVariable Long videoId,
//            @PathVariable String jobId) {
//
//        videoProcessingService.deleteJob(videoId, jobId);
//
//        return ResponseEntity.noContent().build();
//    }
}
