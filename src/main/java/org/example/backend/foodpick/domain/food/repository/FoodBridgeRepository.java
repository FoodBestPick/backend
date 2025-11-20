package org.example.backend.foodpick.domain.food.repository;

import org.example.backend.foodpick.domain.food.model.FoodBridge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodBridgeRepository extends JpaRepository<FoodBridge, Long> {
    void deleteAllByRestaurant_Id(Long restaurantId);
    List<FoodBridge> findAllByRestaurant_Id(Long restaurantId);
    
    void deleteAllByFood_Id(Long foodId);
    List<FoodBridge> findAllByFood_Id(Long foodId);
    
    // ✅ In 검색 메서드 추가
    List<FoodBridge> findAllByFood_IdIn(List<Long> foodIds);
}