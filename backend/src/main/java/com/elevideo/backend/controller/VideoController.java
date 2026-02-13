package com.elevideo.backend.controller;

import com.elevideo.backend.model.Video;
import com.elevideo.backend.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // ✅ Upload simple (solo persistencia por ahora)
    @PostMapping("/upload")
    public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file) {

        // Aquí luego irá la lógica real de almacenamiento
        String fakeUrl = "local-storage/" + file.getOriginalFilename();

        Video video = new Video(fakeUrl);

        Video savedVideo = videoService.save(video);

        return ResponseEntity.ok(savedVideo);
    }

    // ✅ Obtener todos los videos
    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

}
