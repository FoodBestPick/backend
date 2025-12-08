package org.example.backend.foodpick.domain.chat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Builder;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    @Column(nullable = false)
    private Integer targetCount;

    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public ChatRoomEntity(String category, Integer targetCount, LocalDateTime expiresAt) {
        this.category = category;
        this.targetCount = targetCount;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}