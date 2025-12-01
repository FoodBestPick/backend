package org.example.backend.foodpick.domain.food.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodRequest;
import org.example.backend.foodpick.domain.food.dto.FoodResponse;
import org.example.backend.foodpick.domain.food.service.FoodService;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse; // ✅ Import 추가
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/food")
public class FoodController {
    private final FoodService foodService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createFood(@RequestBody FoodRequest request) {
        return foodService.createFood(request);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods() {
        return foodService.getAllFoods();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(@PathVariable(name = "id") Long id) {
        return foodService.getFoodById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFood(@PathVariable(name = "id") Long id, @RequestBody FoodRequest request) {
        return foodService.updateFood(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable(name = "id") Long id) {
        return foodService.deleteFood(id);
    }

    // ✅ [수정] 반환 타입: Page<Restaurant> -> Page<RestaurantResponse>
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> searchByFood(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return foodService.searchRestaurantsByFood(name, pageable);
    }
}