package org.example.backend.foodpick.domain.alarm.repository;

import org.example.backend.foodpick.domain.alarm.model.AlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE AlarmEntity a
        SET a.isRead = true
        WHERE a.receiver.Id = :receiverId
          AND a.isRead = false
    """)
    void readAllByReceiverId(@Param("receiverId") Long receiverId);

    @Modifying
    @Query("""
        DELETE FROM AlarmEntity a
        WHERE a.receiver.Id = :receiverId
    """)
    void deleteAllByReceiverId(@Param("receiverId") Long receiverId);

    long countByReceiverId(Long receiverId);

    List<AlarmEntity> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}