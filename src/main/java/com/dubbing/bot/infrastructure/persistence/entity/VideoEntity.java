package com.dubbing.bot.infrastructure.persistence.entity;

import com.dubbing.bot.domain.model.enums.VideoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_user_id", columnList = "user_id"),
        @Index(name = "idx_videos_status", columnList = "status")
})
public class VideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "youtube_url", nullable = false, length = 512)
    private String youtubeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoStatus status;

    @Column(name = "result_file_path", length = 1024)
    private String resultFilePath;

    @Column(name = "failure_reason", length = 1024)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
