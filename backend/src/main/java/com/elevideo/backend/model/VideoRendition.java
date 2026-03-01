package com.elevideo.backend.model;

import com.elevideo.backend.enums.BackgroundMode;
import com.elevideo.backend.enums.Platform;
import com.elevideo.backend.enums.ProcessingMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_rendition", indexes = {
        @Index(name = "idx_video_rendition_video_id", columnList = "video_id")
})
public class VideoRendition {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String outputUrl;

    @Column
    private String thumbnailUrl;

    @Column
    private String previewUrl;

    @Column
    private Double qualityScore;

    @Column
    private Double durationSeconds;

    @Column
    private Double segmentStart;

    @Column
    private Integer segmentDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingMode processingMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackgroundMode backgroundMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false, updatable = false)
    private Video video;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_job_id", nullable = false, updatable = false)
    private ProcessingJob processingJob;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
