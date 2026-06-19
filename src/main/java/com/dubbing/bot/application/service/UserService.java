package com.dubbing.bot.application.service;

import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.model.enums.UserStatus;
import com.dubbing.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int WELCOME_FREE_MINUTES = 5;

    private final UserRepository userRepository;

    /** Idempotent: returns existing user or registers a new one on first contact. */
    @Transactional
    public User getOrCreate(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    log.info("Registering new user telegramId={}", telegramId);
                    User user = User.builder()
                            .telegramId(telegramId)
                            .balanceMinutes(WELCOME_FREE_MINUTES)
                            .status(UserStatus.ACTIVE)
                            .createdAt(Instant.now())
                            .build();
                    return userRepository.save(user);
                });
    }
}
