package com.dubbing.bot.infrastructure.ai;

import com.dubbing.bot.application.port.SpeechSynthesizer;
import com.dubbing.bot.infrastructure.config.AppProperties;
import com.dubbing.bot.infrastructure.process.ProcessRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Synthesizes a dubbed audio track via the Python helper (tts_synthesize.py),
 * which uses XTTS v2 / Edge-TTS and aligns segments to the SRT timestamps.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonTtsSynthesizer implements SpeechSynthesizer {

    private final AppProperties props;
    private final ProcessRunner processRunner;

    @Override
    public Path synthesize(Path translatedSubtitle, String voice, Path workDir) {
        AppProperties.Tools tools = props.getTools();
        Path output = workDir.resolve("dubbed.wav");

        List<String> command = List.of(
                tools.getPythonPath(),
                Path.of(tools.getPythonDir(), "tts_synthesize.py").toString(),
                "--input", translatedSubtitle.toString(),
                "--output", output.toString(),
                "--voice", voice);

        processRunner.run(command, null, tools.getProcessTimeoutSeconds());
        log.info("TTS synthesis complete -> {}", output);
        return output;
    }
}
