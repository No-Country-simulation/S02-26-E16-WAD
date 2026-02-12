package com.elevideo.backend.model;

import com.elevideo.backend.enums.VideoStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Video() {
        // JPA only
    }

    public Video(String originalUrl) {
        this.originalUrl = originalUrl;
        this.status = VideoStatus.UPLOADED;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter controlado (para el próximo epic)
    public void setStatus(VideoStatus status) {
        this.status = status;
    }
}
