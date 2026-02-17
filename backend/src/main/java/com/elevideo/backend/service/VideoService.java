package com.elevideo.backend.service;

import com.elevideo.backend.dto.video.CreateVideoRequest;
import com.elevideo.backend.dto.video.VideoResponse;
import com.elevideo.backend.dto.video.VideoSearchRequest;
import com.elevideo.backend.dto.video.VideoSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface VideoService {


    VideoResponse createVideo(Long projectId, CreateVideoRequest request);

    /**
     * Obtiene todos los videos del usuario autenticado con paginación y filtros opcionales.
     * Si no se proporcionan filtros en searchParams, retorna todos los videos del usuario.
     *
     * @param searchParams Parámetros de búsqueda, filtrado y paginación (todos opcionales)
     * @return Página de videos con resumen de información
     */
    Page<VideoSummaryResponse> getVideos(Long projectId, VideoSearchRequest searchParams);

    VideoSummaryResponse getVideoById(Long videoId);

    void deleteVideo(Long id);
}

