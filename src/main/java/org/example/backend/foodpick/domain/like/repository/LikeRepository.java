package org.example.backend.foodpick.domain.like.repository;

import org.example.backend.foodpick.domain.like.model.Like;
import org.example.backend.foodpick.domain.like.model.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    boolean existsByUserAndRestaurantReview(Long user, Long restaurantReview);
    void deleteByUserAndRestaurantReview(Long user, Long restaurantReview);
    long countByRestaurantReview(Long restaurantReview);
}
