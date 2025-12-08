package org.example.backend.foodpick.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.like.repository.LikeRepository;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.review.dto.ReviewRequest;
import org.example.backend.foodpick.domain.review.dto.ReviewResponse;
import org.example.backend.foodpick.domain.review.model.Review;
import org.example.backend.foodpick.domain.review.repository.ReviewRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.redis.service.RedisDashboardService;
import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;
    private final JwtTokenValidator jwtTokenValidator;
    private final RedisDashboardService redisDashboardService;

    @Transactional
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(ReviewRequest request, List<MultipartFile> files, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));

        List<String> imageUrls = null;
        if (files != null && !files.isEmpty()) {
            ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
            if (s3Resp != null && s3Resp.getBody() != null) {
                imageUrls = s3Resp.getBody().getData();
            }
        }

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .content(request.getContent())
                .rating(request.getRating())
                .images(imageUrls != null ? imageUrls : request.getImages())
                .build();

        Review savedReview = reviewRepository.save(review);
        
        // 식당 평점/리뷰수 업데이트 로직이 필요하다면 여기에 추가 (또는 별도 배치/이벤트)

        redisDashboardService.recordNewReview(LocalDate.now());

        return ResponseEntity.ok(ApiResponse.success(ReviewResponse.from(savedReview, user.getId(), false, 0)));
    }

    @Transactional
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(Long reviewId, ReviewRequest request, List<MultipartFile> files, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        List<String> imageUrls = request.getImages(); // 기존 이미지 유지 or 수정
        if (files != null && !files.isEmpty()) {
            ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
            if (s3Resp != null && s3Resp.getBody() != null) {
                if (imageUrls == null) imageUrls = new java.util.ArrayList<>();
                imageUrls.addAll(s3Resp.getBody().getData());
            }
        }

        review.update(request.getContent(), request.getRating(), imageUrls);

        boolean isLiked = likeRepository.existsByUserAndRestaurantReview(userId, review.getId());
        long likeCount = likeRepository.countByRestaurantReview(review.getId());

        return ResponseEntity.ok(ApiResponse.success(ReviewResponse.from(review, review.getUser().getId(), isLiked, likeCount)));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteReview(Long reviewId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getRestaurantReviews(Long restaurantId, Pageable pageable, String token) {
        Long currentUserId = -1L;
        if (token != null && !token.isEmpty()) {
            try {
                currentUserId = jwtTokenValidator.getUserId(token);
            } catch (Exception e) {
                // 토큰이 유효하지 않거나 만료된 경우 등은 -1L로 처리하여 비로그인 상태로 간주
                currentUserId = -1L;
            }
        }
        Long finalCurrentUserId = currentUserId;

        Page<Review> reviews = reviewRepository.findAllByRestaurant_Id(restaurantId, pageable);
        Page<ReviewResponse> responses = reviews.map(r -> {
            boolean isLiked = (finalCurrentUserId != -1L) && likeRepository.existsByUserAndRestaurantReview(finalCurrentUserId, r.getId());
            long likeCount = likeRepository.countByRestaurantReview(r.getId());
            return ReviewResponse.from(r, finalCurrentUserId, isLiked, likeCount);
        });
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(Pageable pageable, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        Page<Review> reviews = reviewRepository.findAllByUser_Id(userId, pageable);

        Page<ReviewResponse> responses = reviews.map(review -> {
            boolean isLiked = likeRepository.existsByUserAndRestaurantReview(userId, review.getId());
            long likeCount = likeRepository.countByRestaurantReview(review.getId());
            return ReviewResponse.from(review, userId, isLiked, likeCount);
        });

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
