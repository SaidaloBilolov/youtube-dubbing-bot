package com.dubbing.bot.application.service;

import com.dubbing.bot.application.dto.DubbingJob;
import com.dubbing.bot.application.port.MediaMuxer;
import com.dubbing.bot.application.port.Notifier;
import com.dubbing.bot.application.port.SpeechSynthesizer;
import com.dubbing.bot.application.port.SubtitleDownloader;
import com.dubbing.bot.application.port.Translator;
import com.dubbing.bot.domain.exception.VideoProcessingException;
import com.dubbing.bot.domain.model.Video;
import com.dubbing.bot.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Consumer-side orchestrator: runs the full pipeline
 * (download subtitles -> translate -> synthesize voice -> mux) for one job.
 * Each external step is behind a port, so this class stays pure orchestration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DubbingService {

    private static final int MINUTES_PER_JOB = 1; // simplified; real impl measures duration

    private final VideoRepository videoRepository;
    private final BillingService billingService;
    private final SubtitleDownloader subtitleDownloader;
    private final Translator translator;
    private final SpeechSynthesizer speechSynthesizer;
    private final MediaMuxer mediaMuxer;
    private final Notifier notifier;

    public void process(DubbingJob job) {
        Video video = videoRepository.findById(job.videoId())
                .orElseThrow(() -> new VideoProcessingException("Video not found: " + job.videoId()));

        Path workDir = null;
        boolean charged = false;
        try {
            billingService.charge(job.userId(), MINUTES_PER_JOB);
            charged = true;

            video.markProcessing();
            videoRepository.save(video);
            notifier.sendText(job.chatId(), "\u2699\ufe0f Video qayta ishlanmoqda...");

            workDir = Files.createTempDirectory("dub-" + job.videoId() + "-");

            Path subtitle = subtitleDownloader.download(job.youtubeUrl(), workDir);
            Path translated = translator.translate(subtitle, job.targetLang(), workDir);
            Path dubbedAudio = speechSynthesizer.synthesize(translated, job.voice(), workDir);
            Path sourceVideo = mediaMuxer.downloadVideo(job.youtubeUrl(), workDir);
            Path output = mediaMuxer.mux(sourceVideo, dubbedAudio, workDir);

            video.markDone(output.toString());
            videoRepository.save(video);

            notifier.sendVideo(job.chatId(), output, "\u2705 Tayyor! Dublyaj qilingan video.");
            log.info("Dubbing completed videoId={}", job.videoId());

        } catch (Exception e) {
            log.error("Dubbing failed videoId={}: {}", job.videoId(), e.getMessage(), e);
            video.markFailed(e.getMessage());
            videoRepository.save(video);
            if (charged) {
                // Best-effort refund so a failed job does not cost the user.
                safeRefund(job.userId());
            }
            notifier.sendText(job.chatId(),
                    "\u274c Kechirasiz, videoni qayta ishlashda xatolik yuz berdi. Daqiqalaringiz qaytarildi.");
            throw new VideoProcessingException("Pipeline failed for video " + job.videoId(), e);
        } finally {
            cleanup(workDir);
        }
    }

    private void safeRefund(Long userId) {
        try {
            billingService.refund(userId, MINUTES_PER_JOB);
        } catch (Exception ex) {
            log.error("Refund failed for userId={}: {}", userId, ex.getMessage(), ex);
        }
    }

    private void cleanup(Path workDir) {
        if (workDir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(workDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    log.warn("Could not delete temp file {}", p);
                }
            });
        } catch (IOException e) {
            log.warn("Could not clean work dir {}: {}", workDir, e.getMessage());
        }
    }
}
