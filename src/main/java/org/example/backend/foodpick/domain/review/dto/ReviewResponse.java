package org.example.backend.foodpick.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.review.model.Review;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userProfileImage;
    private Long restaurantId;
    private String content;
    private Double rating;
    private List<String> images;
    private String createdAt;

    @JsonProperty("isMine")
    private boolean isMine;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private long likeCount;

    public static ReviewResponse from(Review review, Long currentUserId, boolean isLiked, long likeCount) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userNickname(review.getUser().getNickname())
                .userProfileImage(review.getUser().getImageUrl())
                .restaurantId(review.getRestaurant().getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(review.getImages())
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .isMine(review.getUser().getId().equals(currentUserId))
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();
    }
}
