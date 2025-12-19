package org.example.backend.foodpick.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponse {

    @Schema(description = "사용자 고유 식별자(ID)", example = "1")
    private Long id;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 닉네임", example = "맛있는녀석들")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/1.png")
    private String image;

    @Schema(description = "누적 경고 횟수", example = "0")
    private int warnings;

    @Schema(description = "사용자 권한", example = "USER", allowableValues = {"USER", "ADMIN"})
    private UserRole role;

    @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "BANNED", "WITHDRAWN"})
    private UserStatus status;

    @Schema(description = "계정 생성일", example = "2023-10-27T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "정보 수정일", example = "2023-10-28T12:00:00")
    private LocalDateTime updatedAt;

    public static UserResponse of(UserEntity user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .image(user.getImageUrl())
                .warnings(user.getWarnings())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}