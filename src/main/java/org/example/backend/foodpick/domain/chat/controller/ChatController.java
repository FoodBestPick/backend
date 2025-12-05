package org.example.backend.foodpick.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.dto.ChatMessageRequest;
import org.example.backend.foodpick.domain.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request) {
        chatService.handleMessage(request);
    }
}
