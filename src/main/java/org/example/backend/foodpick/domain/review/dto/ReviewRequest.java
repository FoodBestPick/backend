package org.example.backend.foodpick.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(
        name = "ReviewRequest",
        description = "리뷰 생성/수정 요청 DTO"
)
@Data
@NoArgsConstructor
public class ReviewRequest {

    @Schema(description = "리뷰를 작성할 맛집 ID", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long restaurantId;

    @Schema(description = "리뷰 내용", example = "분위기 좋고 음식도 맛있어요. 재방문 의사 100%!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double rating;

    @Schema(description = "리뷰 이미지 URL 리스트 (이미 업로드된 이미지 URL)", example = "[\"https://cdn.example.com/reviews/1.jpg\",\"https://cdn.example.com/reviews/2.jpg\"]")
    private List<String> images;
}
