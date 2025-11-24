package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.user.model.UserEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String email;
    private String nickname;
    private String image;
    private String stateMessage;

    public static UserProfileResponse of(UserEntity user) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .image(user.getImageUrl())
                .stateMessage(user.getStateMessage())
                .build();
    }
}
