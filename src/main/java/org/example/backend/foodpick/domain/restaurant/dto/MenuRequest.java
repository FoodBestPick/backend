package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private String menu_name;
    private Integer menu_price; // ✅ 추가
}