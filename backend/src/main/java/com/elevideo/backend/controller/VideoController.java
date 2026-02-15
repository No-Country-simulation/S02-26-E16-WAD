package com.elevideo.backend.controller;

import com.elevideo.backend.model.Video;
import com.elevideo.backend.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }
    
    //prueva cloudinary

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @GetMapping("/test-env")
    public ResponseEntity<String> testEnv() {
        return ResponseEntity.ok("Cloud name: " + cloudName);
    }



    // ✅ Upload simple (simulado)
    @PostMapping("/upload")
    public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file) {

        // Simulación temporal
        String fakeUrl = "local-storage/" + file.getOriginalFilename();
        String fakePublicId = UUID.randomUUID().toString(); // Genera ID único

        Video video = new Video(fakeUrl, fakePublicId);

        Video savedVideo = videoService.save(video);

        return ResponseEntity.ok(savedVideo);
    }

    // ✅ Obtener todos los videos
    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {

        videoService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getById(id));
    }

}
