package org.example.backend.foodpick.domain.like.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.like.service.LikeService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final LikeService likeService;

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable("reviewId") Long reviewId,
            @RequestHeader("Authorization") String token) {
        return likeService.toggleReviewLike(reviewId, token);
    }
}
