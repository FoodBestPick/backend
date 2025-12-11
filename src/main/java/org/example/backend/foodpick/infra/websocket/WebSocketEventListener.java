package org.example.backend.foodpick.infra.websocket;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;
    private final JwtTokenValidator jwtTokenValidator;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // 클라이언트에서 "Authorization" 헤더로 JWT 보내게 할 거라고 가정
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null) return;

        Long userId = jwtTokenValidator.getUserId(token);
        sessionManager.registerUser(userId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // disconnect 쪽은 userId를 직접 가져오기 어려우니,
        // 나중에 필요하면 세션ID 기반으로 확장 가능.
        // 지금은 간단히 두고, 필요하면 로직 추가하자.
    }
}
