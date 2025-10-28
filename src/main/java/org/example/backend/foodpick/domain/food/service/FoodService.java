package org.example.backend.foodpick.domain.food.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodCreateRequest;
import org.example.backend.foodpick.domain.food.model.FoodEntity;
import org.example.backend.foodpick.domain.food.repository.FoodRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    @Transactional
    public ApiResponse<?> createFood(FoodCreateRequest request) {
        // 이미 등록된 식당인지 확인
        if (foodRepository.existsByNameAndAddress(
                request.getRestaurant_name(), 
                request.getRestaurant_address())) {
            return new ApiResponse<>(409, false, "이미 등록된 식당입니다.", null);
        }

        try {
            FoodEntity food = FoodEntity.builder()
                    .name(request.getRestaurant_name())
                    .introduce(request.getRestaurant_introduce())
                    .address(request.getRestaurant_address())
                    .latitude(request.getRestaurant_latitude())
                    .longitude(request.getRestaurant_longtitude())
                    .build();

            foodRepository.save(food);
            return new ApiResponse<>(200, true, "맛집 등록이 완료되었습니다.", null);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, false, "서버 내부에 오류가 발생했습니다.", null);
        }
    }
}
