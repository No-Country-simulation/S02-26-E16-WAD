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

    // (nullable por seguridad en migración)
    @Column
    private Double duration;   // Duración en segundos

    @Column(length = 50)
    private String format;     // mp4, mov, etc

    @Column
    private Integer bytes;     // Tamaño en bytes

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

    // Constructor principal
    public Video(String url, String publicId, Double duration, String format, Integer bytes) {
        this.url = url;
        this.publicId = publicId;
        this.duration = duration;
        this.format = format;
        this.bytes = bytes;
        this.status = VideoStatus.UPLOADED;
    }

    // Se ejecuta automáticamente antes de guardar
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Se ejecuta antes de actualizar
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =======================
    // Getters
    // =======================

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }

    public Double getDuration() {
        return duration;
    }

    public String getFormat() {
        return format;
    }

    public Integer getBytes() {
        return bytes;
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

    // =======================
    // Setter controlado
    // =======================

    public void setStatus(VideoStatus status) {
        this.status = status;
    }
}
