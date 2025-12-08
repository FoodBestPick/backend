package org.example.backend.foodpick.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.like.model.RestaurantLike;
import org.example.backend.foodpick.domain.like.repository.RestaurantLikeRepository;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantLikeService {

    private final RestaurantLikeRepository likeRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> getMyLikedRestaurants(Pageable pageable, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        Page<RestaurantLike> likes = likeRepository.findAllByUser_Id(userId, pageable);

        Page<RestaurantResponse> responses = likes.map(like -> RestaurantResponse.from(like.getRestaurant()));

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    public ResponseEntity<ApiResponse<Void>> toggleLike(Long restaurantId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));
        
        if (likeRepository.existsByUserIdAndRestaurantId(user.getId(), restaurantId)) {
            likeRepository.deleteByUserIdAndRestaurantId(user.getId(), restaurantId);
            return ResponseEntity.ok(ApiResponse.success(null)); // 좋아요 취소
        } else {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
            
            likeRepository.save(RestaurantLike.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .build());
            return ResponseEntity.ok(ApiResponse.success(null)); // 좋아요 추가
        }
    }
}
