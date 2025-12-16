package org.example.backend.foodpick.domain.alarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="alarm_setting")
public class AlarmSettingEntity {

    @EmbeddedId
    private AlarmSettingId id;

    @Column(nullable=false)
    private boolean enabled = true;

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now().withNano(0); }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now().withNano(0); }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static AlarmSettingEntity of(Long userId, AlarmType alarmType, boolean enabled) {
        return AlarmSettingEntity.builder()
                .id(AlarmSettingId.builder()
                        .userId(userId)
                        .alarmType(alarmType)
                        .build())
                .enabled(enabled)
                .build();
    }
}
