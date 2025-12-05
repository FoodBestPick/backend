package org.example.backend.foodpick.domain.favorite.repository;

import org.example.backend.foodpick.domain.favorite.model.Favorite;
import org.example.backend.foodpick.domain.favorite.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    boolean existsByUserAndRestaurant_Id(Long userId, Long restaurantId);
    void deleteByUserAndRestaurant_Id(Long userId, Long restaurantId);
}
