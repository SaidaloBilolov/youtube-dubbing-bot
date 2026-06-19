package com.dubbing.bot.domain.repository;

import com.dubbing.bot.domain.model.Video;

import java.util.Optional;

/**
 * Outbound port for video persistence.
 */
public interface VideoRepository {

    Video save(Video video);

    Optional<Video> findById(Long id);
}
