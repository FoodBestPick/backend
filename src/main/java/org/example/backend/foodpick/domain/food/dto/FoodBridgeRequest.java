package org.example.backend.foodpick.domain.food.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FoodBridgeRequest {
    @NotBlank(message = "대표메뉴 또는 카테고리명을 입력해주세요.")
    private String foodName;
}