package org.example.backend.foodpick.domain.review.repository;

import org.example.backend.foodpick.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByRestaurant_Id(Long restaurantId, Pageable pageable);
    Page<Review> findAllByUser_Id(Long userId, Pageable pageable);
    List<Review> findAllByRestaurant_Id(Long restaurantId);
    void deleteAllByRestaurant_Id(Long restaurantId);
}
