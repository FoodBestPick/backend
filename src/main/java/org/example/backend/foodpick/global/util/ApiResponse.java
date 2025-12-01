package org.example.backend.foodpick.global.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "API 공통 응답 객체")
public class ApiResponse<T> {

    @Schema(description = "상태 코드", example = "200")
    private final int code;

    @Schema(description = "응답 메시지", example = "성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터", nullable = true)
    private final T data;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 생성 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청이 성공적으로 처리되었습니다.", data);
    }

    // 실패 응답 생성 메서드
    public static <T> ApiResponse<T> failure(String message, int code) {
        return new ApiResponse<>(code, message, null);
    }
}