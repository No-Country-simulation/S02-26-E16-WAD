package com.elevideo.backend.service.impl;

import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import com.elevideo.backend.mapper.ProcessingJobMapper;
import com.elevideo.backend.mapper.VideoRenditionMapper;
import com.elevideo.backend.model.ProcessingJob;
import com.elevideo.backend.model.VideoRendition;
import com.elevideo.backend.repository.ProcessingJobRepository;
import com.elevideo.backend.repository.VideoRenditionRepository;
import com.elevideo.backend.service.VideoRenditionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoRenditionServiceImpl implements VideoRenditionService {

    private final VideoRenditionRepository videoRenditionRepository;
    private final VideoRenditionMapper videoRenditionMapper;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingJobMapper processingJobMapper;

    @Transactional
    @Override
    public void handleWebhook(ProcessingJobWebhookRequest request) {

        ProcessingJob job = processingJobRepository
                .findByJobId(request.jobId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProcessingJob no encontrado para jobId: " + request.jobId()
                ));

        processingJobMapper.updateProcessingJob(request,job);

        if (request.isCompleted()) {
            createVideoRendition(job, request);
        }

        processingJobRepository.save(job);

    }

    private void createVideoRendition(ProcessingJob job,ProcessingJobWebhookRequest request){
        VideoRendition rendition = videoRenditionMapper.toVideoRendition(request);
        rendition.setVideo(job.getVideo());
        rendition.setProcessingJob(job);
        rendition.setPlatform(job.getPlatform());

        VideoRendition saved = videoRenditionRepository.save(rendition);

        job.setVideoRendition(saved);

    }
}
