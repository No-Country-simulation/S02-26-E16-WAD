package com.elevideo.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.elevideo.backend.dto.cloudinary.CloudinaryUploadRes;
import com.elevideo.backend.exception.CloudinaryUploadException;
import com.elevideo.backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadRes uploadVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        try {
            Map<String, Object> result = cloudinary.uploader().uploadLarge(
                    file.getBytes(),
                    Map.of(
                            "resource_type", "video",
                            "folder", "Elevideo"
                    )
            );

            return toCloudinaryRes(result);

        } catch (Exception e) {
            throw new CloudinaryUploadException(
                    "Error al subir video: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteVideo(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId no puede ser null o vacío");
        }

        try {
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    Map.of("resource_type", "video")
            );

            String deletionResult = result.get("result").toString();
            if (!"ok".equals(deletionResult)) {
                throw new CloudinaryUploadException(
                        "No se pudo eliminar el video. Resultado: " + deletionResult
                );
            }
        } catch (Exception e) {
            throw new CloudinaryUploadException("Error al eliminar video: " + e.getMessage(), e);
        }
    }



    private CloudinaryUploadRes toCloudinaryRes(Map<String, Object> result) {
        return CloudinaryUploadRes.builder()
                .publicId((String) result.get("public_id"))
                .secureUrl((String) result.get("secure_url"))
                .format((String) result.get("format"))
                .durationInMillis(toDouble(result.get("duration")))
                .sizeInBytes(toLong(result.get("bytes")))
                .width(toInteger(result.get("width")))
                .height(toInteger(result.get("height")))
                .resourceType((String) result.get("resource_type"))
                .build();
    }

    private Double toDouble(Object value) {
        return value == null ? null : Double.valueOf(value.toString());
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Integer toInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }



}
