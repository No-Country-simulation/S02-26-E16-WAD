package com.elevideo.backend.model;

import com.elevideo.backend.enums.VideoStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false, unique = true)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructor protegido requerido por JPA
    protected Video() {
    }

    // Constructor para nuevos videos
    public Video(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
        this.status = VideoStatus.UPLOADED;
    }

    // Se ejecuta automáticamente antes de guardar
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    //  Se ejecuta antes de actualizar
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setter controlado (solo status puede cambiar)
    public void setStatus(VideoStatus status) {
        this.status = status;
    }
}
