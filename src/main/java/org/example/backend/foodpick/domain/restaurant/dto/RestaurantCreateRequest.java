package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.Data;
import java.util.List;

@Data
public class RestaurantCreateRequest {
    private String restaurant_name;
    private String restaurant_introduce;
    private String restaurant_address;
    private Double restaurant_latitude;
    private Double restaurant_longitude;
    
    private String categories; 
    
    private String menus; // JSON String
    
    // ✅ [추가] 운영시간 JSON String 수신용
    private String times; 
    
    private List<String> tags;
}