package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.model.RestaurantPicture;
import org.example.backend.foodpick.domain.tag.dto.TagResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String introduce;
    private String address;
    private String latitude;
    private String longitude;
    private Long count;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<MenuResponse> menus;
    private List<PictureResponse> pictures;
    private List<TagResponse> tags;
    private List<FoodResponse> categories;
    private List<TimeResponse> times;

    // ✅ [추가] 리뷰 수 및 평점
    private Long reviewCount;
    private Double averageRating;

    // ✅ [추가] 프론트엔드 호환성 필드 (이게 있어야 화면에 나옴)
    private String description; // introduce의 별칭
    private List<String> images; // pictures의 URL 리스트 버전
    private String category;    // 대표 카테고리 1개

    // ✅ [추가] 엔티티 -> DTO 변환 메서드 (무한 재귀 방지)
    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .introduce(restaurant.getIntroduce())
                .description(restaurant.getIntroduce()) // 프론트엔드용 매핑
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .count(restaurant.getCount())
                .createdDate(restaurant.getCreatedDate())
                .updatedDate(restaurant.getUpdatedDate())
                .menus(restaurant.getMenus().stream()
                        .map(m -> new MenuResponse(m.getId(), m.getName(), m.getPrice()))
                        .collect(Collectors.toList()))
                .pictures(restaurant.getPictures().stream()
                        .map(p -> new PictureResponse(p.getId(), p.getUrl()))
                        .collect(Collectors.toList()))
                .images(restaurant.getPictures().stream() // 프론트엔드용 이미지 배열
                        .map(RestaurantPicture::getUrl)
                        .collect(Collectors.toList()))
                .tags(restaurant.getTagBridges().stream()
                        .map(tb -> new TagResponse(tb.getTag().getId(), tb.getTag().getName(), tb.getTag().getCategory()))
                        .collect(Collectors.toList()))
                .categories(restaurant.getFoodBridges().stream()
                        .map(fb -> new FoodResponse(fb.getFood().getId(), fb.getFood().getName()))
                        .collect(Collectors.toList()))
                .category(restaurant.getFoodBridges().isEmpty() ? "미지정" : restaurant.getFoodBridges().get(0).getFood().getName()) // 대표 카테고리
                .times(restaurant.getTimes().stream()
                        .map(t -> new TimeResponse(t.getId(), t.getWeek(), t.getStartTime(), t.getEndTime(), t.getRestTime()))
                        .collect(Collectors.toList()))
                .reviewCount(restaurant.getReviewCount() != null ? restaurant.getReviewCount() : 0L)
                .averageRating(restaurant.getAverageRating() != null ? Math.round(restaurant.getAverageRating() * 10) / 10.0 : 0.0)
                .build();
    }
}