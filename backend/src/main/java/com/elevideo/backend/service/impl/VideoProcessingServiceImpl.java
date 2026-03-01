package com.elevideo.backend.service.impl;

import com.elevideo.backend.aspect.LogExecution;
import com.elevideo.backend.client.PythonServiceClient;
import com.elevideo.backend.dto.python.VideoPythonRequest;
import com.elevideo.backend.dto.videoProcess.*;
import com.elevideo.backend.exception.VideoNotFoundException;
import com.elevideo.backend.mapper.ProcessingJobMapper;

import com.elevideo.backend.mapper.VideoProcessingMapper;
import com.elevideo.backend.mapper.VideoRenditionMapper;
import com.elevideo.backend.model.ProcessingJob;
import com.elevideo.backend.model.Video;
import com.elevideo.backend.model.VideoRendition;
import com.elevideo.backend.repository.ProcessingJobRepository;
import com.elevideo.backend.repository.VideoRenditionRepository;
import com.elevideo.backend.repository.VideoRepository;
import com.elevideo.backend.repository.spec.VideoRenditionSpecification;
import com.elevideo.backend.security.CurrentUserProvider;
import com.elevideo.backend.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor

public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final ProcessingJobRepository processingJobRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PythonServiceClient pythonServiceClient;
    private final VideoRepository videoRepository;
    private final ProcessingJobMapper processingJobMapper;
    private final VideoProcessingMapper videoProcessingMapper;
    private final VideoRenditionRepository videoRenditionRepository;
    private final VideoRenditionMapper videoRenditionMapper;


    @LogExecution
    @Override
    public VideoJobCreatedResponse processVideo(Long videoId, VideoProcessRequest request) {

        UUID userId = currentUserProvider.getCurrentUserId();
        Video video = videoRepository.findByIdAndProjectUserId(videoId, userId)
                .orElseThrow(() -> new VideoNotFoundException(
                        "Video no encontrado con id: " + videoId
                ));

        VideoPythonRequest pythonRequest = videoProcessingMapper.toVideoPythonRequest(request, video);

        VideoJobCreatedResponse response = pythonServiceClient.post(
                "/api/video/process",
                pythonRequest,
                VideoJobCreatedResponse.class,
                userId
        );

        ProcessingJob processingJob = processingJobMapper.toProcessingJob(request, response);
        processingJob.setVideo(video);
        processingJobRepository.save(processingJob);

        return response;
    }

    @LogExecution
    @Override
    public JobResponse getJobStatus(Long videoId, String jobId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        ProcessingJob job = processingJobRepository
                .findByJobIdAndVideoIdAndUserId(jobId, videoId, userId)
                .orElseThrow(() -> new VideoNotFoundException("Job no encontrado: " + jobId));


        // Si el job ya terminó (COMPLETED o FAILED), retornar desde BD
//        if (job.getStatus() == JobStatus.COMPLETED
//                || job.getStatus() == JobStatus.FAILED
//                || job.getStatus() == JobStatus.CANCELLED) {
//            return processingJobMapper.toVideoJobStatusResponse(job);
//        }

        // Si el job sigue en progreso (PENDING o PROCESSING),
        // consultar el estado actualizado directamente a Python.
        VideoJobStatusResponse response = pythonServiceClient.get(
                "/api/video/status/" + jobId,
                VideoJobStatusResponse.class,
                userId
        );

        return processingJobMapper.toJobResponse(response);
    }


    /**
     * Cancela un job en progreso.
     * Solo se puede cancelar si el job está en estado pending o processing.
     */
    @LogExecution
    @Override
    public VideoJobCancelResponse cancelJob(Long VideoId, String jobId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        return pythonServiceClient.postEmpty(
                "/api/video/jobs/" + jobId + "/cancel",
                VideoJobCancelResponse.class,
                userId
        );
    }

    @Override
    public Page<VideoRenditionResponse> getVideosRendition(Long videoId, VideoRenditionSearchRequest request
    ) {

        Specification<VideoRendition> spec =
                VideoRenditionSpecification.belongsToVideo(videoId);

        if (request.processingMode() != null) {
            spec = spec.and(
                    VideoRenditionSpecification.hasProcessingMode(request.processingMode())
            );
        }

        if (request.platform() != null) {
            spec = spec.and(
                    VideoRenditionSpecification.hasPlatform(request.platform())
            );
        }

        if (request.backgroundMode() != null) {
            spec = spec.and(
                    VideoRenditionSpecification.hasBackgroundMode(request.backgroundMode())
            );
        }

        Page<VideoRendition> page =
                videoRenditionRepository.findAll(spec, request.toPageable());

        return page.map(videoRenditionMapper::toVideoRenditionResponse);


    }

//    /**
//     * Elimina un job del historial.
//     */
//    @Override
//    public void deleteJob(String jobId) {
//        UUID userId = currentUserProvider.getCurrentUserId();
//
//        pythonServiceClient.delete(
//                "/api/video/jobs/" + jobId,
//                userId
//        );
//    }
//
//    /**
//     * Lista todos los jobs del usuario.
//     * Python filtra automáticamente por el userId del JWT.
//     */
//    @Override
//    public Object listJobs() {
//        UUID userId = currentUserProvider.getCurrentUserId();
//
//        return pythonServiceClient.get(
//                "/api/video/jobs",
//                Object.class,
//                userId
//        );
//    }
}
