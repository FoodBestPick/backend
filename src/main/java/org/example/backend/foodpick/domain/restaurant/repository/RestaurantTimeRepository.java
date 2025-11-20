package org.example.backend.foodpick.domain.restaurant.repository;

import org.example.backend.foodpick.domain.restaurant.model.RestaurantTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTimeRepository extends JpaRepository<RestaurantTime, Long> {
    boolean existsByIdAndRestaurant_Id(Long timeId, Long restaurantId);
    List<RestaurantTime> findAllByRestaurant_Id(Long restaurantId);
    void deleteAllByRestaurant_Id(Long restaurantId);
}