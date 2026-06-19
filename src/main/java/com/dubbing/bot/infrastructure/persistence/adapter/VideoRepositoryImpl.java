package com.dubbing.bot.infrastructure.persistence.adapter;

import com.dubbing.bot.domain.model.Video;
import com.dubbing.bot.domain.repository.VideoRepository;
import com.dubbing.bot.infrastructure.persistence.jpa.VideoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VideoRepositoryImpl implements VideoRepository {

    private final VideoJpaRepository jpa;

    @Override
    public Video save(Video video) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(video)));
    }

    @Override
    public Optional<Video> findById(Long id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }
}
