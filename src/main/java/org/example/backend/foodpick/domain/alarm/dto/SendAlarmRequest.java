package org.example.backend.foodpick.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendAlarmRequest {
    private Long receiverId;
    private AlarmType alarmType;
    private AlarmTargetType targetType;
    private Long targetId;
    private String message;
}
