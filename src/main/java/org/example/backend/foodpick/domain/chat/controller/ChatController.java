package org.example.backend.foodpick.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageRequest;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageResponse;
import org.example.backend.foodpick.domain.chat.service.ChatService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request) {
        chatService.handleMessage(request);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long roomId) {

        return chatService.getMessage(token, roomId);
    }
}
