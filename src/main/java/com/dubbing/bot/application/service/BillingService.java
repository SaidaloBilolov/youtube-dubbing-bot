package com.dubbing.bot.application.service;

import com.dubbing.bot.domain.exception.InsufficientBalanceException;
import com.dubbing.bot.domain.model.Transaction;
import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.repository.TransactionRepository;
import com.dubbing.bot.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Handles balance deductions and refunds. Each balance change is recorded as a
 * Transaction in the same DB transaction to keep the ledger consistent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void charge(Long userId, int minutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (!user.canAfford(minutes)) {
            throw new InsufficientBalanceException(
                    "Balance " + user.getBalanceMinutes() + " < required " + minutes);
        }
        user.deduct(minutes);
        userRepository.save(user);
        recordTransaction(userId, -minutes, "Dubbing charge");
        log.info("Charged userId={} minutes={} remaining={}", userId, minutes, user.getBalanceMinutes());
    }

    @Transactional
    public void refund(Long userId, int minutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.refund(minutes);
        userRepository.save(user);
        recordTransaction(userId, minutes, "Refund (failed job)");
        log.info("Refunded userId={} minutes={}", userId, minutes);
    }

    private void recordTransaction(Long userId, int amount, String description) {
        transactionRepository.save(Transaction.builder()
                .userId(userId)
                .amount(amount)
                .description(description)
                .date(Instant.now())
                .build());
    }
}
