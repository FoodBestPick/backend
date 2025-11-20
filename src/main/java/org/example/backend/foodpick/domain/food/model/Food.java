package org.example.backend.foodpick.domain.food.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.food.model.FoodBridge;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"FOOD\"")
public class Food {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOOD_ID")
    private Long id;
    
    @Column(name = "FOOD_NAME", nullable = false, length = 255)
    private String name;
    
    @Column(name = "FOOD_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "FOOD_UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;
    
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FoodBridge> foodBridges = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // ✅ updateName() 메서드 추가
    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }
}