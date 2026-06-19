package com.dubbing.bot.application.port;

import java.nio.file.Path;

/**
 * Port abstracting subtitle download (yt-dlp). Implemented in infrastructure.
 */
public interface SubtitleDownloader {

    /**
     * Downloads subtitles for the given YouTube URL into the work directory.
     *
     * @return path to the downloaded subtitle file (.srt/.vtt)
     */
    Path download(String youtubeUrl, Path workDir);
}
