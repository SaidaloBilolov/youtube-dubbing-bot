package com.dubbing.bot.infrastructure.process;

import com.dubbing.bot.domain.exception.VideoProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Safe wrapper around external CLI processes (yt-dlp, ffmpeg, python). Enforces a
 * timeout, captures combined output for logging, and surfaces non-zero exit codes
 * as domain exceptions so callers never hang or fail silently.
 */
@Slf4j
@Component
public class ProcessRunner {

    public String run(List<String> command, Path workDir, long timeoutSeconds) {
        return run(command, workDir, timeoutSeconds, Map.of());
    }

    /**
     * @param extraEnv additional environment variables for the child process
     *                 (e.g. secrets). Never logged.
     */
    public String run(List<String> command, Path workDir, long timeoutSeconds, Map<String, String> extraEnv) {
        log.info("Executing: {}", String.join(" ", command));
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command)
                    .redirectErrorStream(true);
            if (workDir != null) {
                pb.directory(workDir.toFile());
            }
            if (extraEnv != null && !extraEnv.isEmpty()) {
                pb.environment().putAll(extraEnv);
            }
            process = pb.start();

            String output = readOutput(process);

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new VideoProcessingException(
                        "Process timed out after " + timeoutSeconds + "s: " + command.get(0));
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Process failed exit={} output={}", exitCode, output);
                throw new VideoProcessingException(
                        "Process exited with " + exitCode + ": " + command.get(0));
            }
            return output;

        } catch (IOException e) {
            throw new VideoProcessingException("Failed to start process: " + command.get(0), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            throw new VideoProcessingException("Process interrupted: " + command.get(0), e);
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
