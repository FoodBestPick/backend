package org.example.backend.foodpick.domain.alarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.user.model.UserEntity;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(name="alarm")
@NoArgsConstructor
@AllArgsConstructor
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    private String message;

    @Enumerated(EnumType.STRING)
    private AlarmTargetType targetType;

    private Long targetId;

    private boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void markAsRead() {
        this.isRead = true;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public static AlarmEntity create(
            UserEntity sender,
            UserEntity receiver,
            AlarmType alarmType,
            AlarmTargetType targetType,
            Long targetId,
            String message
    ) {
        return AlarmEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .alarmType(alarmType)
                .targetType(targetType)
                .targetId(targetId)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
