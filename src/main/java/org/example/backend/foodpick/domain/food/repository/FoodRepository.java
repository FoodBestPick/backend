package org.example.backend.foodpick.domain.food.repository;

import org.example.backend.foodpick.domain.food.model.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByName(String name);
    
    // ✅ 검색 메서드 추가
    Page<Food> findByNameContaining(String name, Pageable pageable);
}
