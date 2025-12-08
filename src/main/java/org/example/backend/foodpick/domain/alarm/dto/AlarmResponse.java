package org.example.backend.foodpick.domain.alarm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlarmResponse {
    private Long id;
    private String message;
    private AlarmType alarmType;
    private AlarmTargetType targetType;
    private Long targetId;
    private boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static AlarmResponse from(AlarmEntity alarm) {
        return AlarmResponse.builder()
                .id(alarm.getId())
                .message(alarm.getMessage())
                .alarmType(alarm.getAlarmType())
                .targetType(alarm.getTargetType())
                .targetId(alarm.getTargetId())
                .createdAt(alarm.getCreatedAt())
                .isRead(alarm.isRead())
                .build();
    }
}
