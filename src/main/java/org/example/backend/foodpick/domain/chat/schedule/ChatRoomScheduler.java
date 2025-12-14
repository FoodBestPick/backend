package org.example.backend.foodpick.domain.chat.schedule;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.repository.ChatRoomRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatRoomScheduler {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void deactivateExpiredRooms() {
        chatRoomRepository.deactivateExpiredRooms(LocalDateTime.now());
    }
}