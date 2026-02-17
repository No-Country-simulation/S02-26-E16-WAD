package com.elevideo.backend.service;

import com.elevideo.backend.dto.cloudinary.CloudinaryUploadRes;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryUploadRes uploadVideo(MultipartFile file);

    void deleteVideo(String publicId);
}
