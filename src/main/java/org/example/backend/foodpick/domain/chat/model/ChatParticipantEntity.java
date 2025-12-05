package org.example.backend.foodpick.domain.chat.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room_participant")
public class ChatParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomEntity room;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Builder
    public ChatParticipantEntity(ChatRoomEntity room, Long userId) {
        this.room = room;
        this.userId = userId;
        this.joinedAt = LocalDateTime.now();
    }
}