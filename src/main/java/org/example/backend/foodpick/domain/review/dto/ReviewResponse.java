package org.example.backend.foodpick.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.review.model.Review;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
        name = "ReviewResponse",
        description = "리뷰 조회/응답 DTO"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "101")
    private Long id;

    @Schema(description = "작성자 유저 ID", example = "7")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "도현")
    private String userNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profiles/user7.png")
    private String userProfileImage;

    @Schema(description = "맛집 ID", example = "12")
    private Long restaurantId;

    @Schema(description = "맛집 이름", example = "홍콩반점")
    private String restaurantName;

    @Schema(description = "리뷰 내용", example = "짜장면이 진짜 맛있어요!")
    private String content;

    @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.0")
    private Double rating;

    @Schema(description = "리뷰 이미지 URL 리스트", example = "[\"https://cdn.example.com/reviews/101-1.jpg\"]")
    private List<String> images;

    @Schema(description = "작성일시 (yyyy-MM-dd HH:mm)", example = "2025-12-22 17:40")
    private String createdAt;

    @JsonProperty("isMine")
    @Schema(description = "내가 작성한 리뷰인지 여부", example = "true")
    private boolean isMine;

    @JsonProperty("isLiked")
    @Schema(description = "내가 좋아요를 눌렀는지 여부", example = "false")
    private boolean isLiked;

    @Schema(description = "좋아요 개수", example = "13")
    private long likeCount;

    public static ReviewResponse from(Review review, Long currentUserId, boolean isLiked, long likeCount) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userNickname(review.getUser().getNickname())
                .userProfileImage(review.getUser().getImageUrl())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
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
