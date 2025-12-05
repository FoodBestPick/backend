package org.example.backend.foodpick.domain.chat.repository;

import org.example.backend.foodpick.domain.chat.model.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
}
