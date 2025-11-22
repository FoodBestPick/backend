package org.example.backend.foodpick.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInKakaoResponse {
    private String email;
    private String nickname;
}
