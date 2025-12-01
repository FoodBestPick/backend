package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.Data;
import java.util.List;

@Data
public class RestaurantUpdateRequest {
    private String restaurant_name;
    private String restaurant_introduce;
    private String restaurant_address;
    private Double restaurant_latitude;
    private Double restaurant_longitude;
    
    // ✅ [수정] 단일 String -> List<String>
    private List<String> categories;
    
    private String menus;
    private String times;
    private List<String> tags;
    private List<Long> delete_picture_ids;
}