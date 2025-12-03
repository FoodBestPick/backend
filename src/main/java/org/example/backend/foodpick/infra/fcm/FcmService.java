package org.example.backend.foodpick.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendNotification(String fcmToken, String title, String body, AlarmEntity alarm) {
        if (fcmToken == null) {
            return;
        }
        try {
            // FCM 메시지 구성
            Notification notification = Notification.builder()
                    .setTitle(title) // 푸시 알림 제목 (예: "새 알림")
                    .setBody(body)   // 푸시 알림 내용 (예: "누가 내 리뷰에 좋아요를 눌렀습니다.")
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken) // 수신자의 디바이스 토큰
                    .setNotification(notification)
                    .putData("alarmId", String.valueOf(alarm.getId())) // 추가 데이터 (앱에서 활용)
                    .putData("targetType", alarm.getTargetType().name())
                    .build();

            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            // System.out.println("FCM 메시지 전송 성공: " + response);
        } catch (Exception e) {
            // 전송 실패 처리 (예: 토큰 만료, 잘못된 토큰 등)
        }
    }
}
