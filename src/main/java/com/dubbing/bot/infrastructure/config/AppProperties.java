package com.dubbing.bot.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed configuration bound from the {@code app.*} properties namespace.
 * Avoids scattering @Value across the codebase.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Telegram telegram = new Telegram();
    private final Gemini gemini = new Gemini();
    private final Tools tools = new Tools();

    @Getter
    @Setter
    public static class Telegram {
        private String botToken;
        private String botUsername;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-1.5-flash";
    }

    @Getter
    @Setter
    public static class Tools {
        private String ytDlpPath = "yt-dlp";
        private String ffmpegPath = "ffmpeg";
        private String pythonPath = "python3";
        private String pythonDir = "python";
        private long processTimeoutSeconds = 1800;
    }
}
