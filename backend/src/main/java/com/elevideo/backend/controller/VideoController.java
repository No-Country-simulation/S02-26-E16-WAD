package com.elevideo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "File is empty")
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize(),
                        "contentType", file.getContentType(),
                        "status", "UPLOADED"
                )
        );
    }
}
