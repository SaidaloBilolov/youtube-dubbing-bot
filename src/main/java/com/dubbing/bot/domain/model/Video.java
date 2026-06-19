package com.dubbing.bot.domain.model;

import com.dubbing.bot.domain.model.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Pure domain model representing a dubbing request for a single YouTube video.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    private Long id;
    private Long userId;
    private String youtubeUrl;
    private VideoStatus status;
    private String resultFilePath;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public void markProcessing() {
        this.status = VideoStatus.PROCESSING;
    }

    public void markDone(String resultFilePath) {
        this.status = VideoStatus.DONE;
        this.resultFilePath = resultFilePath;
    }

    public void markFailed(String reason) {
        this.status = VideoStatus.FAILED;
        this.failureReason = reason;
    }
}
