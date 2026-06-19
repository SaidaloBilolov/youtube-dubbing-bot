package com.dubbing.bot.infrastructure.persistence.jpa;

import com.dubbing.bot.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
}
