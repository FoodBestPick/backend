package org.example.backend.foodpick.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageRequest;
import org.example.backend.foodpick.domain.chat.model.ChatMessageEntity;
import org.example.backend.foodpick.domain.chat.model.ChatRoomEntity;
import org.example.backend.foodpick.domain.chat.repository.ChatMessageRepository;
import org.example.backend.foodpick.domain.chat.repository.ChatRoomRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleMessage(ChatMessageRequest request) {

        ChatRoomEntity room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorException.CHAT_ROOM_NOT_FOUND));

        ChatMessageEntity msg = ChatMessageEntity.builder()
                .room(room)
                .senderId(request.getSenderId())
                .content(request.getContent())
                .build();

        messageRepository.save(msg);

        // 채팅방 전체에 전송
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), request);
    }
}
