package com.elevideo.backend.dto.email;

public record EmailRequest(
        String from,
        String to,
        String subject,
        String html
) {}
