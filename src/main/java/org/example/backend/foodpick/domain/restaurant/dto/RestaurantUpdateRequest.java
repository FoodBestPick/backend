package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUpdateRequest {
    
    private String restaurant_name;
    private String restaurant_introduce;
    private String restaurant_address;
    private String restaurant_latitude;
    private String restaurant_longitude;
    private String restaurant_category;
    
    // ✅ 메뉴 (가격 포함)
    private List<MenuRequest> menus = new ArrayList<>();
    
    // 태그
    private List<String> tags = new ArrayList<>();
    
    // 삭제할 사진 ID 리스트
    private List<Long> delete_picture_ids = new ArrayList<>();
}