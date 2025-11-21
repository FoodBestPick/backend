package org.example.backend.foodpick.domain.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, length = 30, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false, length = 255)
    private String statueMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserType userLoginType;

    @Column(nullable = true, length = 255)
    private String refreshToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static UserEntity ofSignUp(String email, String password, String nickname) {
        return UserEntity.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .imageUrl(null)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVED)
                .statueMessage("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userLoginType(UserType.APP)
                .build();
    }
}
