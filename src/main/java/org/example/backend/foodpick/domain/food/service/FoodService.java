package org.example.backend.foodpick.domain.food.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodRequest;
import org.example.backend.foodpick.domain.food.dto.FoodResponse;
import org.example.backend.foodpick.domain.food.model.Food;
import org.example.backend.foodpick.domain.food.model.FoodBridge;
import org.example.backend.foodpick.domain.food.repository.FoodBridgeRepository;
import org.example.backend.foodpick.domain.food.repository.FoodRepository;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse;
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
            return ResponseEntity.ok(ApiResponse.failure("음식 이름을 입력해주세요.", 400));
        }
        Optional<Food> existing = foodRepository.findByName(request.getName());
        if (existing.isPresent()) {
            return ResponseEntity.ok(ApiResponse.failure("이미 존재하는 음식입니다.", 400));
        }
        Food food = Food.builder().name(request.getName()).build();
        foodRepository.save(food);
        return ResponseEntity.ok(new ApiResponse<>(200, "음식이 생성되었습니다.", null));
    }

    /**
     * 전체 음식 카테고리 목록 조회
     */
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods() {
        List<Food> foods = foodRepository.findAll();
        List<FoodResponse> responses = foods.stream()
                .map(f -> FoodResponse.builder().id(f.getId()).name(f.getName()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(200, "음식 목록", responses));
    }

    /**
     * 음식 카테고리 상세 조회
     */
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("음식을 찾을 수 없습니다.")); // ✅ 안전한 예외 처리
        return ResponseEntity.ok(new ApiResponse<>(200, "음식 상세", FoodResponse.builder().id(food.getId()).name(food.getName()).build()));
    }

    /**
     * 음식 카테고리 수정
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateFood(Long id, FoodRequest request) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("음식을 찾을 수 없습니다.")); // ✅ 안전한 예외 처리
        food.updateName(request.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "음식이 수정되었습니다.", null));
    }

    /**
     * 음식 카테고리 삭제
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("음식을 찾을 수 없습니다.")); // ✅ 안전한 예외 처리
        foodBridgeRepository.deleteAllByFood_Id(id);
        foodRepository.delete(food);
        return ResponseEntity.ok(new ApiResponse<>(200, "대표메뉴가 삭제되었습니다.", null));
    }

    /**
     * ✅ 음식 카테고리명으로 음식점 검색 (페이징) - DTO 반환으로 수정
     */
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> searchRestaurantsByFood(String foodName, Pageable pageable) {
        if (foodName == null || foodName.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 음식 카테고리를 입력해주세요.", 400));
        }
        Page<Food> foodPage = foodRepository.findByNameContaining(foodName, pageable);
        if (foodPage.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "음식 카테고리 검색 결과", Page.empty(pageable)));
        }
        List<Long> foodIds = foodPage.getContent().stream().map(Food::getId).collect(Collectors.toList());
        List<FoodBridge> bridges = foodBridgeRepository.findAllByFood_IdIn(foodIds);
        
        List<RestaurantResponse> responses = bridges.stream()
                .map(FoodBridge::getRestaurant)
                .distinct()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());

        Page<RestaurantResponse> restaurantPage = new PageImpl<>(responses, pageable, foodPage.getTotalElements());
        return ResponseEntity.ok(new ApiResponse<>(200, "음식 카테고리 검색 결과", restaurantPage));
    }
}