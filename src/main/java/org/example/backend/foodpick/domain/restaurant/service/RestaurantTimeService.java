package org.example.backend.foodpick.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.restaurant.dto.TimeRequest;
import org.example.backend.foodpick.domain.restaurant.dto.TimeResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.model.RestaurantTime;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantTimeRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantTimeService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTimeRepository timeRepository;

    // ✅ CREATE
    @Transactional
    public ResponseEntity<ApiResponse<TimeResponse>> addTime(Long restaurantId, TimeRequest req) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElse(null);
        if (restaurant == null) {
            return ResponseEntity.ok(ApiResponse.failure("식당을 찾을 수 없습니다.", 404));
        }

        RestaurantTime time = RestaurantTime.builder()
                .restaurant(restaurant)
                .week(req.getWeek())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .restTime(req.getRestTime())
                .build();

        timeRepository.save(time);

        TimeResponse response = TimeResponse.builder()
                .id(time.getId())
                .week(time.getWeek())
                .startTime(time.getStartTime())
                .endTime(time.getEndTime())
                .restTime(time.getRestTime())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(200, "영업시간이 추가되었습니다.", response));
    }

    // ✅ READ: 모든 영업시간 조회
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TimeResponse>>> getAllTimes(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            return ResponseEntity.ok(ApiResponse.failure("식당을 찾을 수 없습니다.", 404));
        }

        List<TimeResponse> responses = timeRepository.findAllByRestaurant_Id(restaurantId)
                .stream()
                .map(t -> TimeResponse.builder()
                        .id(t.getId())
                        .week(t.getWeek())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .restTime(t.getRestTime())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(200, "영업시간 목록을 조회했습니다.", responses));
    }

    // ✅ UPDATE
    @Transactional
    public ResponseEntity<ApiResponse<TimeResponse>> updateTime(
            Long restaurantId, Long timeId, TimeRequest req) {
        
        if (!timeRepository.existsByIdAndRestaurant_Id(timeId, restaurantId)) {
            return ResponseEntity.ok(ApiResponse.failure("영업시간 정보를 찾을 수 없습니다.", 404));
        }

        RestaurantTime time = timeRepository.findById(timeId).get();
        
        // ⚠️ RestaurantTime에 Setter 필요 또는 Builder 패턴으로 새로 생성 후 저장
        // 현재는 불변 객체이므로 새로 만들어야 함
        RestaurantTime updated = RestaurantTime.builder()
                .id(time.getId())
                .restaurant(time.getRestaurant())
                .week(req.getWeek())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .restTime(req.getRestTime())
                .createdDate(time.getCreatedDate())
                .build();

        timeRepository.save(updated);

        TimeResponse response = TimeResponse.builder()
                .id(updated.getId())
                .week(updated.getWeek())
                .startTime(updated.getStartTime())
                .endTime(updated.getEndTime())
                .restTime(updated.getRestTime())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(200, "영업시간이 수정되었습니다.", response));
    }

    // ✅ DELETE
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteTime(Long restaurantId, Long timeId) {
        if (!timeRepository.existsByIdAndRestaurant_Id(timeId, restaurantId)) {
            return ResponseEntity.ok(ApiResponse.failure("영업시간 정보를 찾을 수 없습니다.", 404));
        }
        timeRepository.deleteById(timeId);
        return ResponseEntity.ok(new ApiResponse<>(200, "영업시간 정보가 삭제되었습니다.", null));
    }
}