package org.example.backend.foodpick.infra.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, Boolean> connectedUsers = new ConcurrentHashMap<>();

    public void registerUser(Long userId) {
        connectedUsers.put(userId, true);
    }

    public void removeUser(Long userId) {
        connectedUsers.remove(userId);
    }

    public void forceLogout(Long userId) {
        if (connectedUsers.containsKey(userId)) {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/force-logout",
                    "정지된 계정입니다. 관리자에 의해 로그아웃되었습니다."
            );
        }
    }
}
