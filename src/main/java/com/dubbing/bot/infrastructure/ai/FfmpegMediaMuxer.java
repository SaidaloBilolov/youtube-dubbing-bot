package com.dubbing.bot.infrastructure.ai;

import com.dubbing.bot.application.port.MediaMuxer;
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
 * Downloads the source video with yt-dlp and merges the dubbed audio with FFmpeg.
 * The original audio is replaced by the generated track; video is copied without
 * re-encoding for speed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegMediaMuxer implements MediaMuxer {

    private final AppProperties props;
    private final ProcessRunner processRunner;

    @Override
    public Path downloadVideo(String youtubeUrl, Path workDir) {
        AppProperties.Tools tools = props.getTools();
        List<String> command = List.of(
                tools.getYtDlpPath(),
                "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                "--merge-output-format", "mp4",
                "-o", workDir.resolve("source.%(ext)s").toString(),
                youtubeUrl);
        processRunner.run(command, workDir, tools.getProcessTimeoutSeconds());
        return findByExtension(workDir, "source.");
    }

    @Override
    public Path mux(Path video, Path dubbedAudio, Path workDir) {
        AppProperties.Tools tools = props.getTools();
        Path output = workDir.resolve("output.mp4");
        List<String> command = List.of(
                tools.getFfmpegPath(),
                "-y",
                "-i", video.toString(),
                "-i", dubbedAudio.toString(),
                "-map", "0:v:0",
                "-map", "1:a:0",
                "-c:v", "copy",
                "-c:a", "aac",
                "-shortest",
                output.toString());
        processRunner.run(command, workDir, tools.getProcessTimeoutSeconds());
        return output;
    }

    private Path findByExtension(Path workDir, String prefix) {
        try (Stream<Path> files = Files.list(workDir)) {
            return files
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .findFirst()
                    .orElseThrow(() -> new VideoProcessingException(
                            "Downloaded video file not found in " + workDir));
        } catch (IOException e) {
            throw new VideoProcessingException("Could not list work dir for video", e);
        }
    }
}
