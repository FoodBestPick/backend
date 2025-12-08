package org.example.backend.foodpick.domain.review.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.review.model.QReview;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QReview review = QReview.review;

    @Override
    public List<Long> countDailyReviewJoins(LocalDate weekStart, LocalDate weekEnd) {

        DateExpression<LocalDate> createdDateOnly = Expressions.dateTemplate(
                LocalDate.class,
                "DATE({0})",
                review.createdAt // Review 엔티티의 createdAt 필드 사용
        );

        List<Tuple> results = queryFactory
                .select(createdDateOnly, review.id.count())
                .from(review)
                .where(review.createdAt.goe(weekStart.atStartOfDay())
                        .and(review.createdAt.loe(weekEnd.atTime(23, 59, 59, 999_999_999))))
                .groupBy(createdDateOnly)
                .orderBy(createdDateOnly.asc())
                .fetch();

        LocalDate current = weekStart;
        Map<LocalDate, Long> dailyCounts = IntStream.range(0, 7)
                .mapToObj(i -> current.plusDays(i))
                .collect(Collectors.toMap(date -> date, date -> 0L));

        // Tuple to Map 변환 (ClassCastException 해결 로직 포함)
        for (Tuple result : results) {

            // java.sql.Date로 추출 후 LocalDate로 변환
            Date sqlDate = result.get(0, Date.class);
            Long count = result.get(1, Long.class);

            if (sqlDate != null && count != null) {
                LocalDate date = sqlDate.toLocalDate();
                dailyCounts.put(date, count);
            }
        }

        // 월요일부터 일요일 순서로 리스트에 담아 반환 (7개 요소 고정)
        return IntStream.range(0, 7)
                .mapToObj(i -> weekStart.plusDays(i))
                .map(dailyCounts::get)
                .collect(Collectors.toList());
    }
}
