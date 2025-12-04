package org.example.backend.foodpick.domain.alarm.repository;

import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {
}
