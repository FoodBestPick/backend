package org.example.backend.foodpick.domain.food.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.food.model.Food;
import org.example.backend.foodpick.domain.food.model.FoodBridge;
import org.example.backend.foodpick.domain.food.repository.FoodBridgeRepository;
import org.example.backend.foodpick.domain.food.repository.FoodRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodBridgeService {
    private final FoodRepository foodRepository;
    private final FoodBridgeRepository foodBridgeRepository;
    private final RestaurantRepository restaurantRepository;

    public ResponseEntity<ApiResponse<Void>> addCategory(Long restaurantId, String foodName) {
        var optR = restaurantRepository.findById(restaurantId);
        if (optR.isEmpty()) return ResponseEntity.ok(ApiResponse.failure("식당을 찾을 수 없습니다.", 404));
        var food = foodRepository.findByName(foodName).orElseGet(() -> foodRepository.save(Food.builder().name(foodName).build()));
        FoodBridge fb = FoodBridge.builder().restaurant(optR.get()).food(food).build();
        foodBridgeRepository.save(fb);
        return ResponseEntity.ok(new ApiResponse<>(200, "대표메뉴(카테고리)가 등록되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<Void>> removeCategory(Long id) {
        if (!foodBridgeRepository.existsById(id)) return ResponseEntity.ok(ApiResponse.failure("연결 정보를 찾을 수 없습니다.", 404));
        foodBridgeRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "대표메뉴(카테고리) 연결이 삭제되었습니다.", null));
    }
}