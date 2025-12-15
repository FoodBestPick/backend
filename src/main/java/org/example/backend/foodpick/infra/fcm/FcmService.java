package org.example.backend.foodpick.infra.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendNotification(String fcmToken, String title, String body, AlarmEntity alarm) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("[FCM] skip: empty token. alarmId={}, targetType={}, title={}",
                    alarm != null ? alarm.getId() : null,
                    alarm != null && alarm.getTargetType() != null ? alarm.getTargetType().name() : null,
                    title);
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title == null ? "" : title)
                    .setBody(body == null ? "" : body)
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putData("alarmId", String.valueOf(alarm.getId()))
                    .putData("targetType", alarm.getTargetType().name())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("[FCM] sent: response={}, token={}, alarmId={}, targetType={}",
                    response,
                    maskToken(fcmToken),
                    alarm.getId(),
                    alarm.getTargetType().name());

        } catch (FirebaseMessagingException e) {
            log.error("[FCM] failed: token={}, alarmId={}, targetType={}, errorCode={}, messagingErrorCode={}, msg={}",
                    maskToken(fcmToken),
                    alarm != null ? alarm.getId() : null,
                    alarm != null && alarm.getTargetType() != null ? alarm.getTargetType().name() : null,
                    e.getErrorCode(),
                    e.getMessagingErrorCode(),
                    e.getMessage(),
                    e
            );
        } catch (Exception e) {
            log.error("[FCM] failed(unexpected): token={}, alarmId={}, targetType={}, msg={}",
                    maskToken(fcmToken),
                    alarm != null ? alarm.getId() : null,
                    alarm != null && alarm.getTargetType() != null ? alarm.getTargetType().name() : null,
                    e.getMessage(),
                    e
            );
        }
    }

    private String maskToken(String token) {
        if (token == null) return "null";
        int len = token.length();
        if (len <= 12) return token;
        return token.substring(0, 6) + "..." + token.substring(len - 4);
    }
}
