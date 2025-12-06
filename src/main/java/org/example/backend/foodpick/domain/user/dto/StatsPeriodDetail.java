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
            long visitors, long prevVisitors,
            long joins, long prevJoins,
            long restaurants, long prevRestaurants,
            long reviews, long prevReviews,
            List<Integer> timeSeries,
            Map<String, Integer> categories,
            List<Integer> ratingDistribution,
            List<TopSearch> topSearches,
            List<PieItem> pie
    ) {
        return StatsPeriodDetail.builder()
                .visitors(visitors)
                .joins(joins)
                .restaurants(restaurants)
                .reviews(reviews)

                .visitorRate(round1(calcRate(prevVisitors, visitors)))
                .joinRate(round1(calcRate(prevJoins, joins)))
                .restaurantRate(round1(calcRate(prevRestaurants, restaurants)))
                .reviewRate(round1(calcRate(prevReviews, reviews)))

                .timeSeries(timeSeries)
                .categories(categories)
                .ratingDistribution(ratingDistribution)
                .topSearches(topSearches)
                .pie(pie)
                .build();
    }

    private static double calcRate(long prev, long curr) {
        if (prev == 0) return curr == 0 ? 0.0 : 100.0;
        return ((double) (curr - prev) / prev) * 100.0;
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
