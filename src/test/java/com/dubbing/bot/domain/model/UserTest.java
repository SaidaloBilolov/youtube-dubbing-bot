package com.dubbing.bot.domain.model;

import com.dubbing.bot.domain.model.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private User user(int balance) {
        return User.builder()
                .id(1L)
                .telegramId(100L)
                .balanceMinutes(balance)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void canAffordWhenBalanceSufficient() {
        assertThat(user(5).canAfford(3)).isTrue();
        assertThat(user(5).canAfford(5)).isTrue();
        assertThat(user(2).canAfford(3)).isFalse();
    }

    @Test
    void deductReducesBalance() {
        User u = user(5);
        u.deduct(2);
        assertThat(u.getBalanceMinutes()).isEqualTo(3);
    }

    @Test
    void deductThrowsWhenInsufficient() {
        assertThatThrownBy(() -> user(1).deduct(2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refundIncreasesBalance() {
        User u = user(1);
        u.refund(4);
        assertThat(u.getBalanceMinutes()).isEqualTo(5);
    }
}
