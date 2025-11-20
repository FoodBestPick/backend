package org.example.backend.foodpick.domain.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.dto.TimeRequest;
import org.example.backend.foodpick.domain.restaurant.dto.TimeResponse;
import org.example.backend.foodpick.domain.restaurant.service.RestaurantTimeService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant/{restaurantId}/times")
public class RestaurantTimeController {
    private final RestaurantTimeService timeService;

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<TimeResponse>> add(
            @PathVariable Long restaurantId,
            @Valid @RequestBody TimeRequest req) {
        return timeService.addTime(restaurantId, req);
    }

    // ✅ READ: 특정 식당의 모든 영업시간 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeResponse>>> getAll(@PathVariable Long restaurantId) {
        return timeService.getAllTimes(restaurantId);
    }

    // ✅ UPDATE: 영업시간 수정
    @PutMapping("/{timeId}")
    public ResponseEntity<ApiResponse<TimeResponse>> update(
            @PathVariable Long restaurantId,
            @PathVariable Long timeId,
            @Valid @RequestBody TimeRequest req) {
        return timeService.updateTime(restaurantId, timeId, req);
    }

    // ✅ DELETE
    @DeleteMapping("/{timeId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long timeId) {
        return timeService.deleteTime(restaurantId, timeId);
    }
}