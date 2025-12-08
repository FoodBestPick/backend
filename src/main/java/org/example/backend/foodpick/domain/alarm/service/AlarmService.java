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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;
    private final JwtTokenValidator jwtTokenValidator;

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

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getMyAlarms(String token) {

        Long userId = jwtTokenValidator.getUserId(token);

        List<AlarmEntity> alarms =
                alarmRepository.findByReceiverIdOrderByCreatedAtDesc(userId);

        List<AlarmResponse> response = alarms.stream()
                .map(AlarmResponse::from)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "내 알림 조회 성공", response));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> readAlarm(String token, Long alarmId) {

        Long userId = jwtTokenValidator.getUserId(token);

        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new CustomException(ErrorException.ALARM_NOT_FOUND));

        if (!alarm.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        if (alarm.isRead()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "이미 읽은 알림입니다.", null));
        }

        alarm.markAsRead();
        alarmRepository.save(alarm);

        return ResponseEntity.ok(new ApiResponse<>(200, "알림을 읽었습니다.", null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> readAllAlarms(String token) {

        Long userId = jwtTokenValidator.getUserId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        alarmRepository.readAllByReceiverId(user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(200, "모든 알림을 읽음 처리했습니다.", null)
        );
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteAlarm(String token, Long alarmId) {

        Long userId = jwtTokenValidator.getUserId(token);

        AlarmEntity alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new CustomException(ErrorException.ALARM_NOT_FOUND));

        if (!alarm.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorException.CAN_NOT_DELETE_ALARM);
        }

        alarmRepository.delete(alarm);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "알림이 삭제되었습니다.", null)
        );
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteAllMyAlarms(String token) {

        Long userId = jwtTokenValidator.getUserId(token);

        long count = alarmRepository.countByReceiverId(userId);

        if (count == 0) {
            return ResponseEntity.ok(new ApiResponse<>(200, "삭제할 알림이 없습니다.", null));
        }

        alarmRepository.deleteAllByReceiverId(userId);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "모든 알림이 삭제되었습니다.", null)
        );
    }
}
