package com.dubbing.bot.infrastructure.ai;

import com.dubbing.bot.application.port.SubtitleDownloader;
import com.dubbing.bot.domain.exception.VideoProcessingException;
import com.dubbing.bot.infrastructure.config.AppProperties;
import com.dubbing.bot.infrastructure.process.ProcessRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Downloads subtitles via yt-dlp. Prefers existing subtitles, falls back to
 * auto-generated captions, and writes them in SRT format into the work dir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YtDlpSubtitleDownloader implements SubtitleDownloader {

    private final AppProperties props;
    private final ProcessRunner processRunner;

    @Override
    public Path download(String youtubeUrl, Path workDir) {
        AppProperties.Tools tools = props.getTools();
        List<String> command = List.of(
                tools.getYtDlpPath(),
                "--skip-download",
                "--write-subs",
                "--write-auto-subs",
                "--sub-format", "vtt",
                "--convert-subs", "srt",
                "--sub-langs", "en.*,en",
                "-o", workDir.resolve("subtitle.%(ext)s").toString(),
                youtubeUrl);

        processRunner.run(command, workDir, tools.getProcessTimeoutSeconds());
        return findSubtitle(workDir);
    }

    private Path findSubtitle(Path workDir) {
        try (Stream<Path> files = Files.list(workDir)) {
            return files
                    .filter(p -> p.toString().endsWith(".srt"))
                    .findFirst()
                    .orElseThrow(() -> new VideoProcessingException(
                            "No subtitles found for video (none available on YouTube)"));
        } catch (IOException e) {
            throw new VideoProcessingException("Could not list work dir for subtitles", e);
        }
    }
}
