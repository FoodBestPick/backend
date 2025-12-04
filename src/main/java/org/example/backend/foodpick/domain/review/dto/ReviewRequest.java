package org.example.backend.foodpick.domain.review.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReviewRequest {
    private Long restaurantId;
    private String content;
    private Double rating;
    private List<String> images; // 이미지 URL 리스트 (이미 업로드된 경우)
}
