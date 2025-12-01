package org.example.backend.foodpick.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDetailResponse {

    private StatsPeriodDetail today;
    private StatsPeriodDetail week;
    private StatsPeriodDetail month;
    private StatsPeriodDetail custom;

    public static UserStatsDetailResponse of(
            StatsPeriodDetail today,
            StatsPeriodDetail week,
            StatsPeriodDetail month,
            StatsPeriodDetail custom
    ) {
        return UserStatsDetailResponse.builder()
                .today(today)
                .week(week)
                .month(month)
                .custom(custom)
                .build();
    }
}
