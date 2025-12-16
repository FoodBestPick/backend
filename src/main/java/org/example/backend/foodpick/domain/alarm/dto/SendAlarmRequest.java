package org.example.backend.foodpick.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendAlarmRequest {
    private Long receiverId;
    private AlarmType alarmType;
    private AlarmTargetType targetType;
    private Long targetId;
    private String message;
}
