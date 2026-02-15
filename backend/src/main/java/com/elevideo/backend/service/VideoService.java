package com.elevideo.backend.service;

import com.elevideo.backend.model.Video;
import com.elevideo.backend.repository.VideoRepository;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video save(Video video) {
        return videoRepository.save(video);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public void delete(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        videoRepository.delete(video);
    }

    public Video getById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

}

