package com.dubbing.bot.infrastructure.persistence.adapter;

import com.dubbing.bot.domain.model.Transaction;
import com.dubbing.bot.domain.repository.TransactionRepository;
import com.dubbing.bot.infrastructure.persistence.jpa.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    @Override
    public Transaction save(Transaction transaction) {
        return PersistenceMapper.toDomain(jpa.save(PersistenceMapper.toEntity(transaction)));
    }
}
