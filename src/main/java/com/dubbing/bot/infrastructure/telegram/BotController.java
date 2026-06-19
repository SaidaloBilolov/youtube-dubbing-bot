package com.dubbing.bot.infrastructure.telegram;

import com.dubbing.bot.application.port.Notifier;
import com.dubbing.bot.application.service.VideoQueueService;
import com.dubbing.bot.domain.exception.InsufficientBalanceException;
import com.dubbing.bot.infrastructure.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Telegram entry point (long-polling) and outbound {@link Notifier}. Handling is
 * intentionally thin: it parses the update, delegates to the queue use-case, and
 * returns immediately. No heavy work happens on the Telegram thread.
 */
@Slf4j
@Component
public class BotController extends TelegramLongPollingBot implements Notifier {

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "https?://(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)\\S+");

    private final AppProperties props;
    private final VideoQueueService videoQueueService;

    public BotController(AppProperties props, VideoQueueService videoQueueService) {
        super(props.getTelegram().getBotToken());
        this.props = props;
        this.videoQueueService = videoQueueService;
    }

    @PostConstruct
    void logStartup() {
        log.info("Telegram bot initialized as @{}", props.getTelegram().getBotUsername());
    }

    @Override
    public String getBotUsername() {
        return props.getTelegram().getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        try {
            if (text.equals("/start")) {
                sendText(chatId, "\ud83d\udc4b Salom! YouTube havolasini yuboring \u2014 men uni o'zbek tiliga dublyaj qilaman.");
                return;
            }
            if (!YOUTUBE_PATTERN.matcher(text).find()) {
                sendText(chatId, "\u26a0\ufe0f Iltimos, to'g'ri YouTube havolasini yuboring.");
                return;
            }

            videoQueueService.enqueue(telegramId, chatId, text);
            sendText(chatId, "\u2705 Qabul qilindi! Videongiz navbatga qo'yildi. Tayyor bo'lganda xabar beraman.");

        } catch (InsufficientBalanceException e) {
            sendText(chatId, "\ud83d\udcb3 Balansingiz yetarli emas. Iltimos, hisobni to'ldiring.");
        } catch (Exception e) {
            log.error("Failed to handle update from chatId={}: {}", chatId, e.getMessage(), e);
            sendText(chatId, "\u274c Kutilmagan xatolik yuz berdi. Birozdan so'ng qayta urinib ko'ring.");
        }
    }

    // --- Notifier port implementation ---

    @Override
    public void sendText(Long chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send text to chatId={}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public void sendVideo(Long chatId, Path videoFile, String caption) {
        try {
            execute(SendVideo.builder()
                    .chatId(chatId.toString())
                    .video(new InputFile(new File(videoFile.toString())))
                    .caption(caption)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send video to chatId={}: {}", chatId, e.getMessage());
        }
    }
}
