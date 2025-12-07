package org.example.backend.foodpick.domain.restaurant.repository;

import org.example.backend.foodpick.domain.restaurant.model.RestaurantSearchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantSearchRepository extends JpaRepository<RestaurantSearchEntity, Integer> {
    @Query("""
        SELECT rs.keyword, SUM(rs.count)
        FROM RestaurantSearchEntity rs
        WHERE rs.createdAt BETWEEN :start AND :end
        GROUP BY rs.keyword
        ORDER BY SUM(rs.count) DESC
    """)
    List<Object[]> findTopKeywordsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
