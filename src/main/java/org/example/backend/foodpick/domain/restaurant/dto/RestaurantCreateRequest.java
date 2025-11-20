package org.example.backend.foodpick.domain.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCreateRequest {
    
    @NotBlank(message = "식당명은 필수입니다")
    private String restaurant_name;
    
    private String restaurant_introduce;
    
    @NotBlank(message = "주소는 필수입니다")
    private String restaurant_address;
    
    private String restaurant_latitude;
    
    private String restaurant_longitude;
    
    private String restaurant_category;
    
    // ✅ 변경: 문자열 배열 → 객체 배열
    private List<MenuRequest> menus = new ArrayList<>();
    
    // 태그는 문자열 배열 유지
    private List<String> tags = new ArrayList<>();
}