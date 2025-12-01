package org.example.backend.foodpick.domain.food.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodBridgeRequest;
import org.example.backend.foodpick.domain.food.service.FoodBridgeService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant/{restaurantId}/category")
public class FoodBridgeController {
    private final FoodBridgeService foodBridgeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(@PathVariable Long restaurantId, @Valid @RequestBody FoodBridgeRequest req) {
        return foodBridgeService.addCategory(restaurantId, req.getFoodName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long restaurantId, @PathVariable Long id) {
        return foodBridgeService.removeCategory(id);
    }
}