package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyPagePasswordRequest {
    private String password;
    private String passwordConfirm;
}
