package com.dubbing.bot.infrastructure.ai;

import com.dubbing.bot.application.port.Translator;
import com.dubbing.bot.infrastructure.config.AppProperties;
import com.dubbing.bot.infrastructure.process.ProcessRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Translates subtitles with Google Gemini Flash by delegating to the Python
 * helper (translate_gemini.py), which preserves SRT timestamps. The API key is
 * passed via environment, never on the command line, to avoid leaking it in logs.
 *
 * <p>Spring Retry guards against transient Gemini errors (rate limits / 5xx).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiTranslator implements Translator {

    private final AppProperties props;
    private final ProcessRunner processRunner;

    @Override
    @Retryable(retryFor = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    public Path translate(Path subtitleFile, String targetLang, Path workDir) {
        AppProperties.Tools tools = props.getTools();
        Path output = workDir.resolve("subtitle.translated.srt");

        List<String> command = new ArrayList<>();
        command.add(tools.getPythonPath());
        command.add(Path.of(tools.getPythonDir(), "translate_gemini.py").toString());
        command.add("--input");
        command.add(subtitleFile.toString());
        command.add("--output");
        command.add(output.toString());
        command.add("--target-lang");
        command.add(targetLang);
        command.add("--model");
        command.add(props.getGemini().getModel());

        // Pass the API key via env so it never appears in process arguments / logs.
        Map<String, String> env = Map.of("GEMINI_API_KEY", props.getGemini().getApiKey());
        processRunner.run(command, null, tools.getProcessTimeoutSeconds(), env);
        log.info("Translation complete -> {}", output);
        return output;
    }
}
