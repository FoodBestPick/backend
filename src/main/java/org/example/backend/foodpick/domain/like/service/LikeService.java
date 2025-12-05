package org.example.backend.foodpick.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.like.model.Like;
import org.example.backend.foodpick.domain.like.repository.LikeRepository;
import org.example.backend.foodpick.domain.review.repository.ReviewRepository;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final JwtTokenValidator jwtTokenValidator;

    public ResponseEntity<ApiResponse<Void>> toggleReviewLike(Long reviewId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        
        // Check if review exists
        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("리뷰를 찾을 수 없습니다.");
        }

        if (likeRepository.existsByUserAndRestaurantReview(userId, reviewId)) {
            likeRepository.deleteByUserAndRestaurantReview(userId, reviewId);
        } else {
            likeRepository.save(Like.builder()
                    .user(userId)
                    .restaurantReview(reviewId)
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
