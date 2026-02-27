package com.elevideo.backend.service.impl;

import com.elevideo.backend.aspect.LogExecution;
import com.elevideo.backend.client.PythonServiceClient;
import com.elevideo.backend.dto.python.VideoProcessingPythonRequest;
import com.elevideo.backend.dto.videoProcess.*;
import com.elevideo.backend.exception.VideoNotFoundException;
import com.elevideo.backend.mapper.ProcessingJobMapper;
import com.elevideo.backend.mapper.PyhtonMapper;
import com.elevideo.backend.model.ProcessingJob;
import com.elevideo.backend.model.Video;
import com.elevideo.backend.repository.ProcessingJobRepository;
import com.elevideo.backend.repository.VideoRepository;
import com.elevideo.backend.security.CurrentUserProvider;
import com.elevideo.backend.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final ProcessingJobRepository processingJobRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PythonServiceClient pythonServiceClient;
    private final VideoRepository videoRepository;
    private final PyhtonMapper pyhtonMapper;
    private final ProcessingJobMapper processingJobMapper;


    /**
     * Convierte el video completo a formato vertical 9:16.
    */
    @LogExecution
    @Override
    public VideoJobCreatedResponse processVertical(Long videoId, VideoVerticalCreateRequest request){
        UUID userId = currentUserProvider.getCurrentUserId();
        Video video = videoRepository.findByIdAndProjectUserId(videoId, userId)
                .orElseThrow(()->new VideoNotFoundException("Video no encontrado con id: "+ videoId));

        VideoProcessingPythonRequest requestBody = pyhtonMapper.toVideoProcessingPythonRequest(request,video.getSecureUrl());

        VideoJobCreatedResponse response = pythonServiceClient.post(
                "/api/video/process",
                requestBody,
                VideoJobCreatedResponse.class,
                userId
        );

        ProcessingJob processingJob = processingJobMapper.toProcessiongJob(request, response);
        processingJob.setVideo(video);
        processingJobRepository.save(processingJob);

        return response;
    }

    /**
     * Genera un short seleccionando automáticamente el mejor segmento.
     * Duración deseada del short en segundos (5-60)
     */
    @Override
    public VideoJobCreatedResponse processShortAuto(ShortAutoProcessRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        return pythonServiceClient.post(
                "/api/video/process",
                request,
                VideoJobCreatedResponse.class,
                userId
        );
    }

    /**
     * Genera un short a partir de un segmento definido manualmente,
     * Duración del segmento en segundos (5-60).
     */
    @Override
    public VideoJobCreatedResponse processShortManual(ShortManualProcessRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        return pythonServiceClient.post(
                "/api/video/process",
                request,
                VideoJobCreatedResponse.class,
                userId
        );
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
    @Override
    public VideoJobStatusResponse cancelJob(String jobId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        return pythonServiceClient.postEmpty(
                "/api/video/jobs/" + jobId + "/cancel",
                VideoJobStatusResponse.class,
                userId
        );
    }

    /**
     * Elimina un job del historial.
     */
    @Override
    public void deleteJob(String jobId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        pythonServiceClient.delete(
                "/api/video/jobs/" + jobId,
                userId
        );
    }

    /**
     * Lista todos los jobs del usuario.
     * Python filtra automáticamente por el userId del JWT.
     */
    @Override
    public Object listJobs() {
        UUID userId = currentUserProvider.getCurrentUserId();

        return pythonServiceClient.get(
                "/api/video/jobs",
                Object.class,
                userId
        );
    }
}
