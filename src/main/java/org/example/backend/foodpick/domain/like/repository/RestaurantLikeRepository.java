package org.example.backend.foodpick.domain.like.repository;

import org.example.backend.foodpick.domain.like.model.RestaurantLike;
import org.example.backend.foodpick.domain.like.model.RestaurantLikeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantLikeRepository extends JpaRepository<RestaurantLike, RestaurantLikeId> {

    @Query("SELECT COUNT(rl) > 0 FROM RestaurantLike rl WHERE rl.user.Id = :userId AND rl.restaurant.id = :restaurantId")
    boolean existsByUserIdAndRestaurantId(@Param("userId") Long userId, @Param("restaurantId") Long restaurantId);

    @Query("DELETE FROM RestaurantLike rl WHERE rl.user.Id = :userId AND rl.restaurant.id = :restaurantId")
    @Modifying
    void deleteByUserIdAndRestaurantId(@Param("userId") Long userId, @Param("restaurantId") Long restaurantId);

    long countByRestaurant_Id(Long restaurantId);
    Page<RestaurantLike> findAllByUser_Id(Long userId, Pageable pageable);
}
