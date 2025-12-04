package org.example.backend.foodpick.domain.alarm.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.dto.AlarmResponse;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.example.backend.foodpick.domain.alarm.repository.AlarmRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.fcm.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;

    public void sendAlarm(Long senderId, SendAlarmRequest request){

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        UserEntity receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        AlarmEntity alarm = AlarmEntity.create(
                sender,
                receiver,
                request.getAlarmType(),
                request.getTargetType(),
                request.getTargetId(),
                request.getMessage()
        );

        alarmRepository.save(alarm);

        AlarmResponse dto = AlarmResponse.from(alarm);

        messagingTemplate.convertAndSend(
                "/topic/alarms/" + receiver.getId(),
                dto
        );

        if (receiver.getFcmToken() != null) {
            String title = "FoodPick 알림";
            String body = request.getMessage();
            fcmService.sendNotification(receiver.getFcmToken(), title, body, alarm);
        }
    }
}
