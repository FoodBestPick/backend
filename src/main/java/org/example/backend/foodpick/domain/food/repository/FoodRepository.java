package org.example.backend.foodpick.domain.food.repository;

import org.example.backend.foodpick.domain.food.model.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    boolean existsByNameAndAddress(String name, String address);
}
