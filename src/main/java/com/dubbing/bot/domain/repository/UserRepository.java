package com.dubbing.bot.domain.repository;

import com.dubbing.bot.domain.model.User;

import java.util.Optional;

/**
 * Outbound port for user persistence. Implemented in the infrastructure layer
 * so the application/domain layers never depend on JPA directly.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByTelegramId(Long telegramId);
}
