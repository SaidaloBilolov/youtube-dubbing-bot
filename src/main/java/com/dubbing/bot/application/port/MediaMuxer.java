package com.dubbing.bot.application.port;

import java.nio.file.Path;

/**
 * Port abstracting media muxing (FFmpeg): downloading the source video and
 * merging the dubbed audio track into the final output file.
 */
public interface MediaMuxer {

    /** Downloads the source video (without audio is acceptable) into workDir. */
    Path downloadVideo(String youtubeUrl, Path workDir);

    /** Merges the dubbed audio with the video, returning the final file. */
    Path mux(Path video, Path dubbedAudio, Path workDir);
}
