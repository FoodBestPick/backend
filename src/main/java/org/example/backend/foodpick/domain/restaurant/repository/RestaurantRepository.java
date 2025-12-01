package org.example.backend.foodpick.domain.restaurant.repository;

import java.util.List;

import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    // ✅ 중복 검사
    boolean existsByNameAndAddress(String name, String address);
    
    // ✅ 이름으로 검색
    Page<Restaurant> findByNameContaining(String name, Pageable pageable);
    
    // ✅ 주소로 검색
    Page<Restaurant> findByAddressContaining(String address, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.menus m " +
           "WHERE (:minPrice IS NULL OR m.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR m.price <= :maxPrice)")
    List<Restaurant> findByMenuPriceRange(@Param("minPrice") Integer minPrice, 
                                          @Param("maxPrice") Integer maxPrice);
}