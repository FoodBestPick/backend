package org.example.backend.foodpick.domain.favorite.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.favorite.model.Favorite;
import org.example.backend.foodpick.domain.favorite.repository.FavoriteRepository;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
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
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;

    public ResponseEntity<ApiResponse<Void>> toggleFavorite(Long restaurantId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));
        
        if (favoriteRepository.existsByUserAndRestaurant_Id(user.getId(), restaurantId)) {
            favoriteRepository.deleteByUserAndRestaurant_Id(user.getId(), restaurantId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
            
            favoriteRepository.save(Favorite.builder()
                    .user(user.getId())
                    .restaurant(restaurant)
                    .build());
            return ResponseEntity.ok(ApiResponse.success(null));
        }
    }
}
