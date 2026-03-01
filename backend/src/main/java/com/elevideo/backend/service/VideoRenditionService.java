package com.elevideo.backend.service;

import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import jakarta.transaction.Transactional;

public interface VideoRenditionService {

    void handleWebhook(ProcessingJobWebhookRequest request);
}
