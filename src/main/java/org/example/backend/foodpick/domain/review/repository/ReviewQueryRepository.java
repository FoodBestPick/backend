package org.example.backend.foodpick.domain.review.repository;

import java.time.LocalDate;
import java.util.List;

public interface ReviewQueryRepository {
    List<Long> countDailyReviewJoins(LocalDate weekStart, LocalDate weekEnd);
}
