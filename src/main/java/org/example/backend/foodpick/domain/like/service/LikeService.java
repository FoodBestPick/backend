package org.example.backend.foodpick.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.domain.like.model.Like;
import org.example.backend.foodpick.domain.like.repository.LikeRepository;
import org.example.backend.foodpick.domain.review.model.Review;
import org.example.backend.foodpick.domain.review.repository.ReviewRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
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
    private final AlarmService alarmService;

    public ResponseEntity<ApiResponse<Void>> toggleReviewLike(Long reviewId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorException.REVIEW_NOT_FOUND));
        Long receiverId = review.getUser().getId();

        // 이미 좋아요면 취소(알림 X)
        if (likeRepository.existsByUserAndRestaurantReview(userId, reviewId)) {
            likeRepository.deleteByUserAndRestaurantReview(userId, reviewId);
            return ResponseEntity.ok(ApiResponse.success(null));
        }

        // 좋아요 추가
        likeRepository.save(Like.builder()
                .user(userId)
                .restaurantReview(reviewId)
                .build());

        if (!receiverId.equals(userId)) {
            SendAlarmRequest req = SendAlarmRequest.builder()
                    .receiverId(receiverId)
                    .alarmType(AlarmType.REVIEW_LIKE)
                    .targetType(AlarmTargetType.REVIEW)
                    .targetId(reviewId)
                    .message("내 리뷰에 좋아요가 눌렸어요.")
                    .build();

            alarmService.sendAlarm(userId, req);
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
