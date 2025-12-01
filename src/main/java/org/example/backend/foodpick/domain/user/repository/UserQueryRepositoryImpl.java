package org.example.backend.foodpick.domain.user.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.user.model.QUserEntity;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory query;
    private final QUserEntity user = QUserEntity.userEntity;

    @Override
    public long countAllUsers() {
        return query
                .select(user.Id.count())
                .from(user)
                .fetchOne();
    }

    @Override
    public List<Long> findAllUserData() {

        DateTemplate<Date> updatedDate =
                Expressions.dateTemplate(
                        Date.class,
                        "DATE({0})",
                        user.createdAt
                );

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = today.plusDays(1).atStartOfDay();

        List<Tuple> result = query
                .select(updatedDate, user.Id.countDistinct())
                .from(user)
                .where(
                        user.updatedAt.goe(startDt),
                        user.updatedAt.lt(endDt)
                )
                .groupBy(updatedDate)
                .orderBy(updatedDate.asc())
                .fetch();

        Map<LocalDate, Long> map = new HashMap<>();
        for (Tuple tuple : result) {
            Date raw = tuple.get(updatedDate);
            LocalDate d = raw.toLocalDate();
            Long count = tuple.get(user.Id.countDistinct());
            map.put(d, count);
        }

        long cumulative = 0;
        List<Long> list = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            cumulative += map.getOrDefault(d, 0L);
            list.add(cumulative);
        }

        return list;
    }
}
