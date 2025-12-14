package org.example.backend.foodpick.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.model.QChatParticipantEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatQueryRepositoryImpl implements ChatQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Long> findMyActiveRoomId(Long userId, LocalDateTime now) {
        QChatParticipantEntity p = QChatParticipantEntity.chatParticipantEntity;

        Long roomId = queryFactory
                .select(p.room.id)
                .from(p)
                .where(
                        p.userId.eq(userId),
                        p.room.isActive.isTrue(),
                        p.room.expiresAt.gt(now)
                )
                .orderBy(p.room.createdAt.desc())
                .fetchFirst();

        return Optional.ofNullable(roomId);
    }
}
