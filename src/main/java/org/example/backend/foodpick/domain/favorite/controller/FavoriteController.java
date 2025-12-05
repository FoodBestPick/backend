package org.example.backend.foodpick.domain.favorite.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.favorite.service.FavoriteService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> toggleFavorite(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        return favoriteService.toggleFavorite(restaurantId, token);
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        return favoriteService.toggleFavorite(restaurantId, token);
    }
}
