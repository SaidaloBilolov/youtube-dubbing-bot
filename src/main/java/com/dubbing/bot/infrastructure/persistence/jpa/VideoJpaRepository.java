package com.dubbing.bot.infrastructure.persistence.jpa;

import com.dubbing.bot.infrastructure.persistence.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoJpaRepository extends JpaRepository<VideoEntity, Long> {
}
