package com.dubbing.bot.infrastructure.persistence.adapter;

import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.repository.UserRepository;
import com.dubbing.bot.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpa;

    @Override
    public User save(User user) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByTelegramId(Long telegramId) {
        return jpa.findByTelegramId(telegramId).map(PersistenceMapper::toDomain);
    }
}
