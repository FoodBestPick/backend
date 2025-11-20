package org.example.backend.foodpick.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantCreateRequest;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantUpdateRequest;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.service.RestaurantService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    
    private final RestaurantService restaurantService;
    
    /**
     * ✅ 맛집 등록
     * POST /restaurant
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @ModelAttribute RestaurantCreateRequest request,
            @RequestPart(required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authorization) {
        
        String token = extractToken(authorization);
        
        try {
            RestaurantResponse response = restaurantService.create(request, files, token);
            return ResponseEntity.ok(new ApiResponse<>(200, "식당 등록 성공", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(500, "서버 오류가 발생했습니다.", null));
        }
    }

    /**
     * 음식점 목록 조회 (페이징)
     * GET /restaurant?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Restaurant>>> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return restaurantService.listRestaurants(pageable);
    }

    /**
     * 음식점 상세 조회
     * GET /restaurant/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Restaurant>> getRestaurant(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    /**
     * 음식점 정보 수정
     * PUT /restaurant/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateRestaurant(
            @PathVariable Long id,
            @ModelAttribute RestaurantUpdateRequest request,
            @RequestPart(required = false) List<MultipartFile> files,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractToken(authorization);
        return restaurantService.updateRestaurant(id, request, files, token);
    }

    /**
     * 음식점 삭제
     * DELETE /restaurant/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = extractToken(authorization);
        return restaurantService.deleteRestaurant(id, token);
    }

    /**
     * 음식점 이름으로 검색
     * GET /restaurant/search/name?name=맛있는&page=0&size=10
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = createPageable(page, size, sort, direction);
        return restaurantService.searchByName(name, pageable);
    }

    /**
     * 주소로 검색
     * GET /restaurant/search/address?address=강남&page=0&size=10
     */
    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByAddress(
            @RequestParam String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = createPageable(page, size, sort, direction);
        return restaurantService.searchByAddress(address, pageable);
    }

    /**
     * Authorization 헤더에서 토큰 추출
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }
}
