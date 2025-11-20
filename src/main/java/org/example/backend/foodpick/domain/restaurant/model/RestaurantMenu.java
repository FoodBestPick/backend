package org.example.backend.foodpick.domain.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"RESTAURANT_MENU\"")
public class RestaurantMenu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANT_MENU_ID")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;
    
    @Column(name = "RESTAURANT_MENU_NAME", nullable = false, length = 255)
    private String name;
    
    // ✅ 가격 필드 추가
    @Column(name = "RESTAURANT_MENU_PRICE")
    private Integer price;
    
    @Column(name = "RESTAURANT_MENU_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "RESTAURANT_MENU_UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;
    
    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}