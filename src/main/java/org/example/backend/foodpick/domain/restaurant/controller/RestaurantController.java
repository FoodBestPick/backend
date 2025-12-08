package org.example.backend.foodpick.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantCreateRequest;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantUpdateRequest;
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
     * 맛집 등록
     * POST /restaurant
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @ModelAttribute RestaurantCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        // 토큰 추출 로직 (간소화)
        String token = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : "";
        
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
     * 맛집 수정
     * PUT /restaurant/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable("id") Long id,
            @ModelAttribute RestaurantUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : "";
        return restaurantService.updateRestaurant(id, request, files, token);
    }

    /**
     * 맛집 삭제
     * DELETE /restaurant/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : "";
        return restaurantService.deleteRestaurant(id, token);
    }

    /**
     * [통합 검색] 프론트엔드 SearchViewModel에서 호출
     * GET /restaurant/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "openNow", required = false) Boolean openNow,
            @RequestParam(value = "sort", required = false, defaultValue = "rating") String sort
    ) {
        return restaurantService.searchRestaurants(keyword, category, tags, minPrice, maxPrice, minRating, openNow, sort);
    }

    /**
     * 전체 목록 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> listRestaurants(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return restaurantService.listRestaurants(pageable);
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurant(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : "";
        return restaurantService.getRestaurantById(id, token);
    }
}
