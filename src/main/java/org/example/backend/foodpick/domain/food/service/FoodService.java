package org.example.backend.foodpick.domain.food.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodRequest;
import org.example.backend.foodpick.domain.food.dto.FoodResponse;
import org.example.backend.foodpick.domain.food.model.Food;
import org.example.backend.foodpick.domain.food.model.FoodBridge;
import org.example.backend.foodpick.domain.food.repository.FoodBridgeRepository;
import org.example.backend.foodpick.domain.food.repository.FoodRepository;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodBridgeRepository foodBridgeRepository;

    /**
     * 음식 카테고리 생성
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> createFood(FoodRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("음식명을 입력해주세요.", 400));
        }

        Optional<Food> existing = foodRepository.findByName(request.getName());
        if (existing.isPresent()) {
            return ResponseEntity.ok(ApiResponse.failure("이미 존재하는 음식입니다.", 409));
        }

        Food food = Food.builder()
                .name(request.getName())
                .build();
        foodRepository.save(food);
        
        return ResponseEntity.ok(new ApiResponse<>(200, "음식이 생성되었습니다.", null));
    }

    /**
     * 전체 음식 카테고리 목록 조회
     */
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods() {
        List<Food> foods = foodRepository.findAll();
        
        List<FoodResponse> responses = foods.stream()
                .map(f -> FoodResponse.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(200, "음식 목록", responses));
    }

    /**
     * 음식 카테고리 상세 조회
     */
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(Long id) {
        Optional<Food> foodOpt = foodRepository.findById(id);
        if (foodOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("음식을 찾을 수 없습니다.", 404));
        }

        Food food = foodOpt.get();
        FoodResponse response = FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .build();
        
        return ResponseEntity.ok(new ApiResponse<>(200, "음식 조회", response));
    }

    /**
     * 음식 카테고리 수정
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateFood(Long id, FoodRequest request) {
        Optional<Food> foodOpt = foodRepository.findById(id);
        if (foodOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("음식을 찾을 수 없습니다.", 404));
        }

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("음식명을 입력해주세요.", 400));
        }

        Food food = foodOpt.get();
        food.updateName(request.getName());
        foodRepository.save(food);
        
        return ResponseEntity.ok(new ApiResponse<>(200, "음식이 수정되었습니다.", null));
    }

    /**
     * 음식 카테고리 삭제
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteFood(Long id) {
        Optional<Food> foodOpt = foodRepository.findById(id);
        if (foodOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("음식을 찾을 수 없습니다.", 404));
        }

        // 연관된 FoodBridge 먼저 삭제
        foodBridgeRepository.deleteAllByFood_Id(id);
        
        // Food 삭제
        foodRepository.deleteById(id);
        
        return ResponseEntity.ok(new ApiResponse<>(200, "음식이 삭제되었습니다.", null));
    }

    /**
     * ✅ 음식 카테고리명으로 음식점 검색 (페이징)
     */
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchRestaurantsByFood(String foodName, Pageable pageable) {
        if (foodName == null || foodName.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 음식 카테고리를 입력해주세요.", 400));
        }

        // 1. 음식 카테고리 검색 (LIKE 검색)
        Page<Food> foodPage = foodRepository.findByNameContaining(foodName, pageable);
        
        if (foodPage.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "음식 카테고리 검색 결과", Page.empty(pageable)));
        }

        // 2. 검색된 음식 ID 리스트 추출
        List<Long> foodIds = foodPage.getContent().stream()
                .map(Food::getId)
                .collect(Collectors.toList());
        
        // 3. FoodBridge를 통해 연결된 Restaurant 조회
        List<FoodBridge> bridges = foodBridgeRepository.findAllByFood_IdIn(foodIds);
        
        if (bridges.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "음식 카테고리 검색 결과", Page.empty(pageable)));
        }
        
        // 4. Restaurant 중복 제거
        List<Restaurant> restaurants = bridges.stream()
                .map(FoodBridge::getRestaurant)
                .distinct()
                .collect(Collectors.toList());

        // 5. Page 객체 생성
        Page<Restaurant> restaurantPage = new PageImpl<>(
                restaurants,
                pageable,
                foodPage.getTotalElements()
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "음식 카테고리 검색 결과", restaurantPage));
    }
}