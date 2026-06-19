package com.dubbing.bot.application.dto;

import java.io.Serializable;

/**
 * Message payload placed on the RabbitMQ queue. Kept small and serializable:
 * it carries identifiers only, not heavy state. The consumer re-loads entities
 * from the database using these ids.
 */
public record DubbingJob(
        Long videoId,
        Long userId,
        Long chatId,
        String youtubeUrl,
        String targetLang,
        String voice
) implements Serializable {
}
