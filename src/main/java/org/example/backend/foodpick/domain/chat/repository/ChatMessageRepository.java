package org.example.backend.foodpick.domain.chat.repository;

import org.example.backend.foodpick.domain.chat.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity,Long> {
}
