package com.elevideo.backend.service.impl;

import com.elevideo.backend.aspect.LogExecution;
import com.elevideo.backend.dto.cloudinary.CloudinaryUploadRes;
import com.elevideo.backend.dto.video.CreateVideoRequest;
import com.elevideo.backend.dto.video.VideoResponse;
import com.elevideo.backend.dto.video.VideoSearchRequest;
import com.elevideo.backend.dto.video.VideoSummaryResponse;
import com.elevideo.backend.exception.ForbiddenException;
import com.elevideo.backend.exception.ProjectNotFoundException;
import com.elevideo.backend.exception.VideoNotFoundException;
import com.elevideo.backend.mapper.VideoMapper;
import com.elevideo.backend.model.Project;
import com.elevideo.backend.model.Video;
import com.elevideo.backend.repository.ProjectRepository;
import com.elevideo.backend.repository.UserRepository;
import com.elevideo.backend.repository.VideoRepository;
import com.elevideo.backend.security.CurrentUserProvider;
import com.elevideo.backend.service.CloudinaryService;
import com.elevideo.backend.service.VideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private final CloudinaryService cloudinaryService;

    private final CurrentUserProvider currentUserProvider;

    private final VideoMapper videoMapper;

    @LogExecution
    @Override
    @Transactional
    public VideoResponse createVideo(Long projectId,CreateVideoRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Proyecto no encontrado con id: " + projectId));

        CloudinaryUploadRes uploadRes = cloudinaryService.uploadVideo(request.video());

        try {
            Video video = videoMapper.toVideo(request.title(), uploadRes);
            video.setProject(project);
            videoRepository.save(video);

            return videoMapper.toVideoResponse(video);
        } catch (Exception ex) {
            cloudinaryService.deleteVideo(uploadRes.publicId());
            throw ex;
        }
    }

    @LogExecution
    @Override
    public Page<VideoSummaryResponse> getVideos(Long projectId, VideoSearchRequest searchParams) {
        UUID userId = currentUserProvider.getCurrentUserId();

        // Validar que el proyecto existe y pertenece al usuario
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Proyecto no encontrado con id: " + projectId));

        if (!project.getUser().getId().equals(userId)) {
            throw new ForbiddenException("No tienes permiso para ver los videos de este proyecto");
        }

        String formattedSearch = searchParams.searchTerm() == null || searchParams.searchTerm().isBlank()
                ? null
                : "%" + searchParams.searchTerm().trim() + "%";

        // Buscar videos del proyecto
        Pageable pageable = searchParams.toPageable();
        Page<Video> videos = videoRepository.findProjectVideos(
                projectId,
                formattedSearch,
                searchParams.status(),
                pageable
        );

        return videos.map(videoMapper::toVideoSummaryResponse);
    }

    @LogExecution
    @Override
    public VideoSummaryResponse getVideoById(Long videoId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        // Buscar el video
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("Video no encontrado con id: " + videoId));

        // Validar que el video pertenece a un proyecto del usuario
        if (!video.getProject().getUser().getId().equals(userId)) {
            throw new ForbiddenException("No tienes permiso para ver este video");
        }

        return videoMapper.toVideoSummaryResponse(video);
    }

    @LogExecution
    @Override
    @Transactional
    public void deleteVideo(Long videoId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("Video no encontrado con id: " + videoId));

        // Validar que el video pertenece a un proyecto del usuario
        if (!video.getProject().getUser().getId().equals(userId)) {
            throw new ForbiddenException("No tienes permiso para eliminar este video");
        }

        cloudinaryService.deleteVideo(video.getPublicId());
        videoRepository.delete(video);
    }
}
