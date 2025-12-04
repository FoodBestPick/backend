package org.example.backend.foodpick.domain.like.repository;

import org.example.backend.foodpick.domain.like.model.RestaurantLike;
import org.example.backend.foodpick.domain.like.model.RestaurantLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantLikeRepository extends JpaRepository<RestaurantLike, RestaurantLikeId> {
    boolean existsByUser_IdAndRestaurant_Id(Long userId, Long restaurantId);
    void deleteByUser_IdAndRestaurant_Id(Long userId, Long restaurantId);
    long countByRestaurant_Id(Long restaurantId);
}
