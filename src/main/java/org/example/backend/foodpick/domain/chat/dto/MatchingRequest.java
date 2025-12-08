package org.example.backend.foodpick.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingRequest {

    // 위도(latitude), 경도(longitude)
    private Double latitude;
    private Double longitude;

    private Integer targetCount;

    private String category;
}
