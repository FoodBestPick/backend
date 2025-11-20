package org.example.backend.foodpick.domain.restaurant.repository;

import org.example.backend.foodpick.domain.restaurant.model.RestaurantPicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantPictureRepository extends JpaRepository<RestaurantPicture, Long> {
    List<RestaurantPicture> findAllByRestaurant_Id(Long restaurantId);
    Optional<RestaurantPicture> findByIdAndRestaurant_Id(Long id, Long restaurantId);
    void deleteAllByRestaurant_Id(Long restaurantId); // ✅ 추가
}