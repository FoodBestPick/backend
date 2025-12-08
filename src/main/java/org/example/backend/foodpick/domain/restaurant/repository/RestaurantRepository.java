package org.example.backend.foodpick.domain.restaurant.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.user.dto.PieItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    // ✅ 중복 검사
    boolean existsByNameAndAddress(String name, String address);
    
    // ✅ 이름으로 검색
    Page<Restaurant> findByNameContaining(String name, Pageable pageable);
    
    // ✅ 주소로 검색
    Page<Restaurant> findByAddressContaining(String address, Pageable pageable);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.menus m " +
           "WHERE (:minPrice IS NULL OR m.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR m.price <= :maxPrice)")
    List<Restaurant> findByMenuPriceRange(@Param("minPrice") Integer minPrice, 
                                          @Param("maxPrice") Integer maxPrice);

    @Query("SELECT new org.example.backend.foodpick.domain.user.dto.PieItem(f.name, COUNT(r)) " +
            "FROM FoodBridge fb JOIN fb.restaurant r JOIN fb.food f " +
            "GROUP BY f.name")
    List<PieItem> countRestaurantsByCategory();

    @Query("""
    SELECT new org.example.backend.foodpick.domain.user.dto.PieItem(f.name, COUNT(r))
    FROM FoodBridge fb
    JOIN fb.restaurant r
    JOIN fb.food f
    WHERE r.createdDate BETWEEN :start AND :end
    GROUP BY f.name
""")
    List<PieItem> countRestaurantsByCategoryBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}