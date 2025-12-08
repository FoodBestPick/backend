package org.example.backend.foodpick.domain.chat.repository;

import org.example.backend.foodpick.domain.chat.model.ChatParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, Long> {
}
