package org.example.backend.foodpick.domain.auth.dto;

import lombok.Getter;
import org.example.backend.foodpick.global.util.ApiResponse;

@Getter
public class AuthInResponse<T> extends ApiResponse<T> {
    private final Object user;

    public AuthInResponse(int code, String message, T data, Object user) {
        super(code, message, data);
        this.user = user;
    }
}
