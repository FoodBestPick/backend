package org.example.backend.foodpick.domain.chat.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_message")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomEntity room;

    @Column(nullable = false)
    private Long senderId;

    @Column(length=255)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().withNano(0);
    }

    @Builder
    public ChatMessageEntity(ChatRoomEntity room, Long senderId, String content) {
        this.room = room;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}