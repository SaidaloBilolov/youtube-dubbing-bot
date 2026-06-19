package com.dubbing.bot.domain.repository;

import com.dubbing.bot.domain.model.Transaction;

/**
 * Outbound port for transaction persistence.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);
}
