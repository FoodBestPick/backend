package org.example.backend.foodpick.domain.food.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"FOOD_BRIDGE\"")
public class FoodBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ✅ DB 스키마: FOOD_BRIDGE (ID 아님)
    @Column(name = "FOOD_BRIDGE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_ID", nullable = false)
    private Food food;
}