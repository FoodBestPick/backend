package org.example.backend.foodpick.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.domain.chat.dto.MatchingRequest;
import org.example.backend.foodpick.domain.chat.dto.MatchingResponse;
import org.example.backend.foodpick.domain.chat.model.ChatParticipantEntity;
import org.example.backend.foodpick.domain.chat.model.ChatRoomEntity;
import org.example.backend.foodpick.domain.chat.repository.ChatParticipantRepository;
import org.example.backend.foodpick.domain.chat.repository.ChatRoomRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.redis.service.RedisChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final JwtTokenValidator jwtTokenValidator;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    private final AlarmService alarmService;
    private final RedisChatService redisChatService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final double MATCH_RADIUS_KM = 5.0;

    @Transactional
    public ResponseEntity<ApiResponse<MatchingResponse>> requestMatch(
            String token,
            MatchingRequest request
    ) {

        Long userId = jwtTokenValidator.getUserId(token);

        if (redisChatService.isMatching(userId)) {
            throw new CustomException(ErrorException.ALREADY_MATCHING);
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));


        double lat = request.getLatitude();
        double lng = request.getLongitude();

        int targetCount = request.getTargetCount() == null ? 2 : request.getTargetCount();
        if (targetCount < 2 || targetCount > 10)
            throw new CustomException(ErrorException.INVALID_MATCHING_PERSON);

        String categoryKey = resolveCategory(request.getCategory());
        String queueKey = redisChatService.buildQueueKey(categoryKey, targetCount);

        redisChatService.saveLocation(userId, lat, lng);

        redisChatService.enqueueUser(queueKey, userId);

        redisChatService.saveUserQueue(userId, queueKey);

        Set<Long> nearUsers =
                redisChatService.findUsersInRadius(lat, lng, MATCH_RADIUS_KM);

        List<Long> matched =
                redisChatService.matchFromQueue(queueKey, nearUsers, targetCount);

        if (matched == null) {
            return ResponseEntity.ok(new ApiResponse<>(200, "매칭 대기 중입니다.", new MatchingResponse(false, null)));
        }

        // 채팅방 생성
        ChatRoomEntity room = ChatRoomEntity.builder()
                .category(categoryKey)
                .targetCount(targetCount)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        chatRoomRepository.save(room);

        // 참여자 저장
        matched.forEach(uid -> {
            chatParticipantRepository.save(
                    ChatParticipantEntity.builder()
                            .room(room)
                            .userId(uid)
                            .build()
            );
        });

        sendMatchNotifications(matched, room, categoryKey);

        return ResponseEntity.ok(new ApiResponse<>(200, "매칭이 완료되었습니다.", new MatchingResponse(true, room.getId())));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> cancelMatch(String token) {

        Long userId = jwtTokenValidator.getUserId(token);

        redisChatService.cancelMatching(userId);

        return ResponseEntity.ok(new ApiResponse<>(200, "매칭이 취소되었습니다.", null));
    }

    private void sendMatchNotifications(List<Long> userIds,
                                        ChatRoomEntity room,
                                        String category) {

        for (Long uid : userIds) {

            String message = "매칭이 완료되었습니다! 채팅방을 확인해주세요";

            SendAlarmRequest req = new SendAlarmRequest(
                    uid,
                    AlarmType.MATCH_SUCCESS,
                    AlarmTargetType.CHAT_ROOM,
                    room.getId(),
                    message
            );

            messagingTemplate.convertAndSendToUser(
                    uid.toString(),
                    "/queue/match-complete",
                    new MatchingResponse(true, room.getId())
            );

            alarmService.sendAlarm(uid, req);
        }
    }

    private String resolveCategory(String category) {
        if (category == null || category.isBlank()) return "ANY";
        return category.toUpperCase();
    }
}