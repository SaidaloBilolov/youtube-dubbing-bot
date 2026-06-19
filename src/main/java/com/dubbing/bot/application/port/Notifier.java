package com.dubbing.bot.application.port;

import java.nio.file.Path;

/**
 * Outbound port for notifying the user about job progress / results.
 * Implemented by the Telegram adapter in infrastructure. Declaring it here keeps
 * the consumer (application layer) decoupled from the Telegram SDK.
 */
public interface Notifier {

    void sendText(Long chatId, String message);

    void sendVideo(Long chatId, Path videoFile, String caption);
}
