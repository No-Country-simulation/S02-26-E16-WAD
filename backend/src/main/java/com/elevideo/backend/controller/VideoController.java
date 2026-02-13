package com.elevideo.backend.controller;

import com.elevideo.backend.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public ResponseEntity<?> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "archivo vacio")
            );
        }

        if (!file.getContentType().startsWith("video/")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Tipo de archivo invalido")
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Archivo muy grande")
            );
        }

        String videoId = UUID.randomUUID().toString();

        return ResponseEntity.ok(
                Map.of(
                        "videoId", videoId,
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize(),
                        "contentType", file.getContentType(),
                        "status", "UPLOADED"
                )
        );
    }
}

