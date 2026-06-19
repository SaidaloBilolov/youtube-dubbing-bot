package com.dubbing.bot.application.port;

import java.nio.file.Path;

/**
 * Port abstracting text-to-speech synthesis (XTTS v2 / Edge-TTS).
 */
public interface SpeechSynthesizer {

    /**
     * Synthesizes a dubbed audio track from a translated subtitle file,
     * aligning speech segments to subtitle timestamps.
     *
     * @return path to the generated audio file (e.g. .wav)
     */
    Path synthesize(Path translatedSubtitle, String voice, Path workDir);
}
