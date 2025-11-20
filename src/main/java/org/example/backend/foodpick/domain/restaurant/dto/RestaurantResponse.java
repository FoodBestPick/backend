package org.example.backend.foodpick.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.food.dto.FoodResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
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

    // ✅ from() 메서드 추가
    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .introduce(restaurant.getIntroduce())
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .count(restaurant.getCount())
                .createdDate(restaurant.getCreatedDate())
                .updatedDate(restaurant.getUpdatedDate())
                .menus(restaurant.getMenus().stream()
                        .map(m -> MenuResponse.builder()
                                .id(m.getId())
                                .name(m.getName())
                                .price(m.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .pictures(restaurant.getPictures().stream()
                        .map(p -> PictureResponse.builder()
                                .id(p.getId())
                                .url(p.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .tags(restaurant.getTagBridges().stream()
                        .map(tb -> TagResponse.builder()
                                .id(tb.getTag().getId())
                                .name(tb.getTag().getName())
                                .build())
                        .collect(Collectors.toList()))
                .categories(restaurant.getFoodBridges().stream()
                        .map(fb -> FoodResponse.builder()
                                .id(fb.getFood().getId())
                                .name(fb.getFood().getName())
                                .build())
                        .collect(Collectors.toList()))
                .times(restaurant.getTimes().stream()
                        .map(t -> TimeResponse.builder()
                                .id(t.getId())
                                .week(t.getWeek())
                                .startTime(t.getStartTime())
                                .endTime(t.getEndTime())
                                .restTime(t.getRestTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}