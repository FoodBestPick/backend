package org.example.backend.foodpick.domain.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TimeRequest {
    @NotBlank(message = "요일 정보를 입력해주세요.")
    private String week;

    @NotBlank(message = "시작 시간을 입력해주세요.")
    private String startTime;

    @NotBlank(message = "종료 시간을 입력해주세요.")
    private String endTime;

    @NotBlank(message = "휴게 시간 정보를 입력해주세요.")
    private String restTime;
}