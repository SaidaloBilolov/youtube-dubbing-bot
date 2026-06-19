package com.dubbing.bot.application.port;

import java.nio.file.Path;

/**
 * Port abstracting subtitle translation (Google Gemini 1.5 Flash).
 * Implementations must preserve subtitle timing cues.
 */
public interface Translator {

    /**
     * Translates a subtitle file, preserving timestamps.
     *
     * @param subtitleFile source subtitle file
     * @param targetLang   target language code (e.g. "uz")
     * @param workDir      working directory for output
     * @return path to the translated subtitle file
     */
    Path translate(Path subtitleFile, String targetLang, Path workDir);
}
