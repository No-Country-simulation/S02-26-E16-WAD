package com.elevideo.backend.service;

import com.elevideo.backend.model.Video;
import com.elevideo.backend.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    // Crear y guardar video desde resultado de Cloudinary
    public Video createAndSave(Map<String, Object> uploadResult) {

        String url = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        Double duration = uploadResult.get("duration") != null
                ? Double.valueOf(uploadResult.get("duration").toString())
                : null;

        String format = uploadResult.get("format") != null
                ? uploadResult.get("format").toString()
                : null;

        Integer bytes = uploadResult.get("bytes") != null
                ? Integer.valueOf(uploadResult.get("bytes").toString())
                : null;

        Video video = new Video(url, publicId, duration, format, bytes);

        return videoRepository.save(video);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Video getById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

    public void delete(Video video) {
        videoRepository.delete(video);
    }
}
