package com.dubbing.bot.application.dto;

import java.nio.file.Path;

/**
 * Result of a completed dubbing pipeline run.
 */
public record DubbingResult(
        Long videoId,
        Long chatId,
        Path outputFile,
        int durationMinutes
) {
}
