package org.example.backend.foodpick.domain.review.repository;

import org.example.backend.foodpick.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByRestaurant_Id(Long restaurantId, Pageable pageable);
    List<Review> findAllByRestaurant_Id(Long restaurantId);
    void deleteAllByRestaurant_Id(Long restaurantId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT FLOOR(r.rating), COUNT(r)
    FROM Review r
    WHERE r.createdAt BETWEEN :start AND :end
    GROUP BY FLOOR(r.rating)
""")
    List<Object[]> countRatingDistributionBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
