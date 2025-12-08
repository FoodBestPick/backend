package org.example.backend.foodpick.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.review.dto.ReviewRequest;
import org.example.backend.foodpick.domain.review.dto.ReviewResponse;
import org.example.backend.foodpick.domain.review.service.ReviewService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestPart(value = "data") String data,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token) {
        try {
            ReviewRequest request = objectMapper.readValue(data, ReviewRequest.class);
            return reviewService.createReview(request, files, token);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable("id") Long id,
            @RequestPart(value = "data") String data,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token) {
        try {
            ReviewRequest request = objectMapper.readValue(data, ReviewRequest.class);
            return reviewService.updateReview(id, request, files, token);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 수정 중 오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String token) {
        return reviewService.deleteReview(id, token);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getRestaurantReviews(
            @PathVariable("restaurantId") Long restaurantId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return reviewService.getRestaurantReviews(restaurantId, pageable, token);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String token) {
        return reviewService.getMyReviews(pageable, token);
    }
}
