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

    @Column(length = 255)
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

    @Column(length = 255)
    private String statusMessage;

    @Column(length = 255)
    private String stateMessage;

    @Column(nullable = false)
    private int warnings = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserType userLoginType;

    @Column(length = 255)
    private String refreshToken;

    @Column(length = 255)
    private String fcmToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ban_end_at")
    private LocalDateTime banEndAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.updatedAt = LocalDateTime.now().withNano(0);
        this.banEndAt = LocalDateTime.now().withNano(0);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public void updateWarning(int warnings, String message) {
        this.warnings = warnings;
        this.statusMessage = message;
    }

    public void updateStatus(UserStatus status, LocalDateTime BanEndAt) {
        this.status = status;
        this.banEndAt = BanEndAt.withNano(0);
    }

    public void updateMessage(String message) {
        this.statusMessage = message;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateProfile(String nickname, String stateMessage){
        this.nickname = nickname;
        this.stateMessage = stateMessage;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void clearBan() {
        this.status = UserStatus.ACTIVED;
        this.banEndAt = null;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public static UserEntity ofSignUp(String email, String password, String nickname) {
        return UserEntity.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .imageUrl(null)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVED)
                .warnings(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userLoginType(UserType.APP)
                .build();
    }

    public static UserEntity signInOauth(String email, String nickname, UserType userLoginType) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(null)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVED)
                .warnings(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userLoginType(userLoginType)
                .build();
    }
}
