package org.example.backend.foodpick.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageRequest;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageResponse;
import org.example.backend.foodpick.domain.chat.dto.ChatRoomResponse;
import org.example.backend.foodpick.domain.chat.model.ChatMessageEntity;
import org.example.backend.foodpick.domain.chat.model.ChatRoomEntity;
import org.example.backend.foodpick.domain.chat.repository.ChatMessageRepository;
import org.example.backend.foodpick.domain.chat.repository.ChatQueryRepository;
import org.example.backend.foodpick.domain.chat.repository.ChatRoomRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatQueryRepository chatQueryRepository;
    private final JwtTokenValidator jwtTokenValidator;

    public void handleMessage(ChatMessageRequest request) {

        ChatRoomEntity room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorException.CHAT_ROOM_NOT_FOUND));

        UserEntity sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (room.getExpiresAt().isBefore(LocalDateTime.now()) || Boolean.FALSE.equals(room.getIsActive())) {
            throw new CustomException(ErrorException.CHAT_ROOM_EXPIRED);
        }

        ChatMessageEntity msg = ChatMessageEntity.builder()
                .room(room)
                .senderId(sender.getId())
                .content(request.getContent())
                .build();

        messageRepository.save(msg);

        String formattedTime = formatKoreanTime(msg.getCreatedAt());

        ChatMessageResponse response = new ChatMessageResponse(
                room.getId(),
                sender.getId(),
                sender.getNickname(),
                sender.getImageUrl(),
                msg.getContent(),
                formattedTime
        );

        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), response);
    }

    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessage(String token, Long roomId) {

        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorException.CHAT_ROOM_NOT_FOUND));

        List<ChatMessageEntity> logs = messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        List<ChatMessageResponse> messages = logs.stream().map(msg -> {

            UserEntity sender = userRepository.findById(msg.getSenderId())
                    .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

            return new ChatMessageResponse(
                    room.getId(),
                    sender.getId(),
                    sender.getNickname(),
                    sender.getImageUrl(),
                    msg.getContent(),
                    formatKoreanTime(msg.getCreatedAt())
            );
        }).toList();

        return ResponseEntity.ok(
                new ApiResponse<>(200, "메시지 가져오기 성공", messages)
        );
    }

    public ResponseEntity<ApiResponse<ChatRoomResponse>> getMyActiveRoom(String token) {
        Long userId = jwtTokenValidator.getUserId(token);

        Long roomId = chatQueryRepository
                .findMyActiveRoomId(userId, LocalDateTime.now())
                .orElse(null);

        ChatRoomResponse response = new ChatRoomResponse(roomId);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "내 활성 채팅방 조회 성공", response)
        );
    }

    public String formatKoreanTime(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
        return time.format(formatter).replace("AM", "오전").replace("PM", "오후");
    }
}
