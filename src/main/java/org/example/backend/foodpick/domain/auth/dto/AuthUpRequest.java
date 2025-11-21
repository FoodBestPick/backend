package org.example.backend.foodpick.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUpRequest {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
}
