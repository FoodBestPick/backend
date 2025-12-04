package org.example.backend.foodpick.domain.like.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.like.service.RestaurantLikeService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class RestaurantLikeController {

    private final RestaurantLikeService likeService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        return likeService.toggleLike(restaurantId, token);
    }
    
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        // toggleLike handles both add and remove, but if frontend calls DELETE explicitly:
        return likeService.toggleLike(restaurantId, token);
    }
}
