package com.dubbing.bot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Pure domain model for a balance change (top-up or deduction).
 * A positive amount is a credit, a negative amount is a debit (minutes).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long id;
    private Long userId;
    private Integer amount;
    private String description;
    private Instant date;
}
