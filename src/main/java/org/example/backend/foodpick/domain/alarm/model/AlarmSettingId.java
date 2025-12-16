package org.example.backend.foodpick.domain.alarm.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class AlarmSettingId implements Serializable {
    private Long userId;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;
}
