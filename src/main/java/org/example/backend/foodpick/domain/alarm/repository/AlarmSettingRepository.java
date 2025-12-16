package org.example.backend.foodpick.domain.alarm.repository;

import org.example.backend.foodpick.domain.alarm.model.AlarmSettingEntity;
import org.example.backend.foodpick.domain.alarm.model.AlarmSettingId;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmSettingRepository extends JpaRepository<AlarmSettingEntity, AlarmSettingId> {
    List<AlarmSettingEntity> findAllByIdUserId(Long userId);
    Optional<AlarmSettingEntity> findByIdUserIdAndIdAlarmType(Long userId, AlarmType alarmType);
}
