package org.example.backend.foodpick.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatQueryRepository {
    Optional<Long> findMyActiveRoomId(Long userId, LocalDateTime now);
}
