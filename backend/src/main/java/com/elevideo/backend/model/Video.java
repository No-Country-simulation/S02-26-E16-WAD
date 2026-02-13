package com.elevideo.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 🔒 Constructor protegido requerido por JPA
    protected Video() {
    }

    // ✅ Constructor público para crear nuevos videos
    public Video(String originalUrl) {
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
        this.status = VideoStatus.UPLOADED;
    }

    public Long getId() {
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

    public void setStatus(VideoStatus status) {
        this.status = status;
    }
}
