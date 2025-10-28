package org.example.backend.foodpick.domain.food.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.food.dto.FoodCreateRequest;
import org.example.backend.foodpick.domain.food.service.FoodService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public ApiResponse<?> createFood(@RequestBody FoodCreateRequest request) {
        return foodService.createFood(request);
    }
}
