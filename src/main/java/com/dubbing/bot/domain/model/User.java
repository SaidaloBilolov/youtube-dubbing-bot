package com.dubbing.bot.domain.model;

import com.dubbing.bot.domain.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Pure domain model for a bot user. Framework-agnostic: contains no JPA / Spring
 * annotations so the business rules stay independent of infrastructure.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private Long telegramId;
    private Integer balanceMinutes;
    private UserStatus status;
    private Instant createdAt;

    /** Business rule: a user can afford a job only if balance covers it. */
    public boolean canAfford(int minutes) {
        return balanceMinutes != null && balanceMinutes >= minutes;
    }

    public void deduct(int minutes) {
        if (!canAfford(minutes)) {
            throw new IllegalStateException("Balance cannot go negative");
        }
        this.balanceMinutes -= minutes;
    }

    public void refund(int minutes) {
        this.balanceMinutes += minutes;
    }
}
