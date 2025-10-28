package org.example.backend.foodpick.domain.food.dto;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
public class FoodCreateRequest {
    
    @NotBlank(message = "식당의 이름을 입력해주세요.")
    private String restaurant_name;
    
    private String restaurant_introduce;
    
    @NotBlank(message = "식당의 주소를 입력해주세요.")
    private String restaurant_address;
    
    @NotBlank(message = "식당의 좌표를 입력해주세요.")
    private String restaurant_latitude;
    
    @NotBlank(message = "식당의 좌표를 입력해주세요.")
    private String restaurant_longtitude;
    
    @NotBlank(message = "허용되지 않은 카테고리입니다.")
    private String restaurant_category;
}
