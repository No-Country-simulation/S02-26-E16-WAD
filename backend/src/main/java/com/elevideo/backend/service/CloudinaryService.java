package com.elevideo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private String lastPublicId;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ✅ Subir video
    public Map<String, Object> uploadVideo(MultipartFile file) throws IOException {

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "video",
                        "folder", "elevideo"
                )
        );

        // Guardamos public_id para usarlo después
        lastPublicId = uploadResult.get("public_id").toString();

        return uploadResult;
    }

    // ✅ Obtener último public_id (opcional si lo necesitas)
    public String getLastPublicId() {
        return lastPublicId;
    }

    // ✅ Eliminar video de Cloudinary
    public void deleteVideo(String publicId) throws IOException {

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "video")
        );
    }
}
