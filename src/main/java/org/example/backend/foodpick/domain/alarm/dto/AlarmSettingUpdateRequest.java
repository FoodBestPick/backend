package org.example.backend.foodpick.domain.alarm.dto;

import lombok.Getter;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;

@Getter
public class AlarmSettingUpdateRequest {
    private AlarmType alarmType;
    private boolean enabled;
}
