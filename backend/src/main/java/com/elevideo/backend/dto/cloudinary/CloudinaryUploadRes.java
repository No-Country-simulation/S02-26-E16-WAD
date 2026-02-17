package com.elevideo.backend.dto.cloudinary;

import lombok.Builder;

@Builder
public record CloudinaryUploadRes(
        String publicId,
        String secureUrl,
        String format,
        Long sizeInBytes,
        Double durationInMillis,
        Integer width,
        Integer height,
        String resourceType
) {}
