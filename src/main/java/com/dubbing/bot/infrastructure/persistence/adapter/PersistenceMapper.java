package com.dubbing.bot.infrastructure.persistence.adapter;

import com.dubbing.bot.domain.model.Transaction;
import com.dubbing.bot.domain.model.User;
import com.dubbing.bot.domain.model.Video;
import com.dubbing.bot.infrastructure.persistence.entity.TransactionEntity;
import com.dubbing.bot.infrastructure.persistence.entity.UserEntity;
import com.dubbing.bot.infrastructure.persistence.entity.VideoEntity;

/**
 * Maps between pure domain models and JPA entities. Keeping mapping in one place
 * preserves the boundary between the domain and the persistence framework.
 */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .telegramId(e.getTelegramId())
                .balanceMinutes(e.getBalanceMinutes())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }

    static UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId())
                .telegramId(u.getTelegramId())
                .balanceMinutes(u.getBalanceMinutes())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }

    static Video toDomain(VideoEntity e) {
        return Video.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .youtubeUrl(e.getYoutubeUrl())
                .status(e.getStatus())
                .resultFilePath(e.getResultFilePath())
                .failureReason(e.getFailureReason())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    static VideoEntity toEntity(Video v) {
        return VideoEntity.builder()
                .id(v.getId())
                .userId(v.getUserId())
                .youtubeUrl(v.getYoutubeUrl())
                .status(v.getStatus())
                .resultFilePath(v.getResultFilePath())
                .failureReason(v.getFailureReason())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    static Transaction toDomain(TransactionEntity e) {
        return Transaction.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .amount(e.getAmount())
                .description(e.getDescription())
                .date(e.getDate())
                .build();
    }

    static TransactionEntity toEntity(Transaction t) {
        return TransactionEntity.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .date(t.getDate())
                .build();
    }
}
