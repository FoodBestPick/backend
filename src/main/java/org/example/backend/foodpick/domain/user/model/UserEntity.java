package org.example.backend.foodpick.domain.user.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false, length = 255)
    private String statueMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 10)
    private UserType userLoginType;

}
