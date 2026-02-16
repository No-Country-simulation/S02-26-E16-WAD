package com.elevideo.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;
    private final CloudinaryService cloudinaryService;

    public VideoController(VideoService videoService,
                           CloudinaryService cloudinaryService) {
        this.videoService = videoService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {

        Map<String, Object> result = cloudinaryService.uploadVideo(file);

        Video savedVideo = videoService.createAndSave(result);

        return ResponseEntity.ok(savedVideo);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) throws IOException {

        Video video = videoService.getById(id);

        cloudinaryService.deleteVideo(video.getPublicId());

        videoService.delete(video);

        return ResponseEntity.noContent().build();
    }
}
