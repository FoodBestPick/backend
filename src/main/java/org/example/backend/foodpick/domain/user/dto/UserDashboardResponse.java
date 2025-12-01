package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardResponse {

    private long users;
    private int restaurants;
    private int todayReviews;
    private int weekReviews;
    private int monthReviews;

    private int[] allUserData;
    private int[] weekUserData;
    private int[] barData;
    private PieItem[] pieData;

    public static UserDashboardResponse create(
            long users,
            int restaurants,
            int todayReviews,
            int weekReviews,
            int monthReviews,
            int[] allUserData,
            int[] weekUserData,
            int[] barData,
            PieItem[] pieData
    ) {
        return UserDashboardResponse.builder()
                .users(users)
                .restaurants(restaurants)
                .todayReviews(todayReviews)
                .weekReviews(weekReviews)
                .monthReviews(monthReviews)
                .allUserData(allUserData)
                .weekUserData(weekUserData)
                .barData(barData)
                .pieData(pieData)
                .build();
    }
}
