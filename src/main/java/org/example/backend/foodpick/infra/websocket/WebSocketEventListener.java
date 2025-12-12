package org.example.backend.foodpick.infra.websocket;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final WebSocketSessionManager sessionManager;
    private final JwtTokenValidator jwtTokenValidator;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String token = accessor.getFirstNativeHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
        }

        if (token == null || token.isBlank()) {
            log.warn("WebSocket Connection Rejected: Authorization header missing or empty. Session ID: {}", accessor.getSessionId());
            // 토큰이 없으면 연결 종료
            return;
        }

        try {
            Long userId = jwtTokenValidator.getUserId(token);
            sessionManager.registerUser(userId);

            log.info("WebSocket Connection Success: User {} connected with Session ID: {}", userId, accessor.getSessionId());

        } catch (JwtException e) {
            log.error("WebSocket Connection Rejected: Invalid JWT Token for Session ID: {}. Error: {}",
                    accessor.getSessionId(), e.getMessage());

        } catch (Exception e) {
            log.error("WebSocket Connection Rejected: Unexpected Error for Session ID: {}. Error: {}",
                    accessor.getSessionId(), e.toString());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    }
}
