package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsPeriodDetail {

    private long visitors;
    private long joins;
    private long restaurants;
    private long reviews;
    private double visitorRate;
    private double joinRate;
    private double restaurantRate;
    private double reviewRate;

    private List<Integer> timeSeries;
    private Map<String, Integer> categories;
    private List<Integer> ratingDistribution;
    private List<TopSearch> topSearches;
    private List<PieItem> pie;

    public static StatsPeriodDetail ofUserStats(
            long visitors,
            long joins,
            List<Integer> timeSeries
    ) {
        return StatsPeriodDetail.builder()
                .visitors(visitors)
                .joins(joins)

                .restaurants(0)
                .reviews(0)
                .visitorRate(0)
                .joinRate(0)
                .restaurantRate(0)
                .reviewRate(0)
                .timeSeries(timeSeries)
                .categories(Map.of(
                        "한식", 50,
                        "중식", 20,
                        "일식", 15,
                        "양식", 10,
                        "기타", 5
                ))
                .ratingDistribution(List.of(10, 20, 30, 40, 50))
                .topSearches(List.of(
                        new TopSearch("마라탕", 1000),
                        new TopSearch("강남역 맛집", 800)
                ))
                .pie(List.of(
                        new PieItem("검색", 40),
                        new PieItem("SNS", 25),
                        new PieItem("광고", 20),
                        new PieItem("기타", 15)
                ))
                .build();
    }
}
