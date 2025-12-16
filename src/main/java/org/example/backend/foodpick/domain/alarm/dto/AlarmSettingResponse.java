package org.example.backend.foodpick.domain.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSettingResponse {
    private Map<AlarmType, Boolean> settings;
}
