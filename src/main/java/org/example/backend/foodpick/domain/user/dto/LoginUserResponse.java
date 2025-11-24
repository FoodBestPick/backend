package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserResponse {
    private Long id;
    private String email;
    private String nickname;
    private boolean isAdmin;
}
