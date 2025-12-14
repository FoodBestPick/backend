package org.example.backend.foodpick.infra.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userToSessions = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> connectedUsers = new ConcurrentHashMap<>();

    public void registerSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return;

        sessionToUser.put(sessionId, userId);
        userToSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        connectedUsers.put(userId, true);
    }

    public void removeSession(String sessionId) {
        if (sessionId == null) return;

        Long userId = sessionToUser.remove(sessionId);
        if (userId == null) return;

        Set<String> sessions = userToSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userToSessions.remove(userId);
                connectedUsers.remove(userId);
            }
        } else {
            connectedUsers.remove(userId);
        }
    }

    public void removeUser(Long userId) {
        if (userId == null) return;

        Set<String> sessions = userToSessions.remove(userId);
        if (sessions != null) {
            for (String sid : sessions) {
                sessionToUser.remove(sid);
            }
        }
        connectedUsers.remove(userId);
    }

    public void forceLogout(Long userId) {
        // 기존 방식 유지: 온라인일 때만 보내기 :contentReference[oaicite:2]{index=2}
        if (connectedUsers.containsKey(userId)) {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/force-logout",
                    "정지된 계정입니다. 관리자에 의해 로그아웃되었습니다."
            );
        }
    }
}
