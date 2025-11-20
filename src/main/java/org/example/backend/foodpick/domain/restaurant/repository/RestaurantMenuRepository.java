package org.example.backend.foodpick.domain.restaurant.repository;

import org.example.backend.foodpick.domain.restaurant.model.RestaurantMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantMenuRepository extends JpaRepository<RestaurantMenu, Long> {
    void deleteAllByRestaurant_Id(Long restaurantId);
    List<RestaurantMenu> findAllByRestaurant_Id(Long restaurantId);
    Optional<RestaurantMenu> findByRestaurant_IdAndName(Long restaurantId, String name);
    Page<RestaurantMenu> findByNameContaining(String name, Pageable pageable);
}