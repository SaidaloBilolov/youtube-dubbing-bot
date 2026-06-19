package com.dubbing.bot.application.service;

import com.dubbing.bot.application.dto.DubbingJob;
import com.dubbing.bot.application.port.JobPublisher;
import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.model.Video;
import com.dubbing.bot.domain.model.enums.UserStatus;
import com.dubbing.bot.domain.model.enums.VideoStatus;
import com.dubbing.bot.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

/**
 * Producer side of the Producer-Consumer model. Validates the request, persists
 * a PENDING video row, then enqueues a lightweight job. Returns immediately so
 * the Telegram thread is never blocked by heavy processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoQueueService {

    private static final String DEFAULT_TARGET_LANG = "uz";
    private static final String DEFAULT_VOICE = "uz-UZ-MadinaNeural";

    private final UserService userService;
    private final VideoRepository videoRepository;
    private final JobPublisher jobPublisher;

    /**
     * Enqueues a new dubbing request.
     *
     * @return the persisted Video (status PENDING)
     */
    @Transactional
    public Video enqueue(Long telegramId, Long chatId, String youtubeUrl) {
        User user = userService.getOrCreate(telegramId);
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("User is blocked");
        }

        Video video = videoRepository.save(Video.builder()
                .userId(user.getId())
                .youtubeUrl(youtubeUrl)
                .status(VideoStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        DubbingJob job = new DubbingJob(
                video.getId(),
                user.getId(),
                chatId,
                youtubeUrl,
                DEFAULT_TARGET_LANG,
                DEFAULT_VOICE);

        // Publish only AFTER the DB commit, otherwise a fast consumer could read the
        // video row before this transaction is visible (or process a row that later
        // rolls back). This is a lightweight alternative to a full outbox pattern.
        publishAfterCommit(job);
        log.info("Enqueued dubbing job videoId={} userId={}", video.getId(), user.getId());
        return video;
    }

    private void publishAfterCommit(DubbingJob job) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    jobPublisher.publish(job);
                }
            });
        } else {
            jobPublisher.publish(job);
        }
    }
}
