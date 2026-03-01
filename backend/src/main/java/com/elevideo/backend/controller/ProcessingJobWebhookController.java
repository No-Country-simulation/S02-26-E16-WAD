package com.elevideo.backend.controller;

import com.elevideo.backend.dto.webhook.ProcessingJobWebhookRequest;
import com.elevideo.backend.service.VideoRenditionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal")
@Tag(name = "06 - Processing Webhooks",
        description = "Endpoints internos para recepción de webhooks relacionados con el procesamiento asíncrono de videos y actualización del estado de jobs."
)
public class ProcessingJobWebhookController {

    private final VideoRenditionService videoRenditionService;

    @PostMapping("/processing-jobs/webhook")
    public ResponseEntity<?> webhookProcessingVideo(@RequestBody ProcessingJobWebhookRequest request){
        videoRenditionService.handleWebhook(request);
        return ResponseEntity.ok().build();
    }
}
