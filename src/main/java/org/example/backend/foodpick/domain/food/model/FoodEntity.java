package org.example.backend.foodpick.domain.food.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"RESTAURANT\"")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class FoodEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANT_ID")
    private Long id;

    @Column(name = "RESTAURANT_NAME", nullable = false)
    private String name;

    @Column(name = "RESTAURANT_INTRODUCE")
    private String introduce;

    @Column(name = "RESTAURANT_ADDRESS", nullable = false)
    private String address;

    @Column(name = "RESTAURANT_COUNT", nullable = false)
    private Long count;

    @Column(name = "RESTAURANT_LATITUDE", nullable = false)
    private String latitude;

    @Column(name = "RESTAURANT_LONGITUDE", nullable = false)
    private String longitude;

    @Column(name = "RESTAURANT_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "RESTAURANT_UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;

    @Builder
    public FoodEntity(String name, String introduce, String address, 
                     String latitude, String longitude) {
        this.name = name;
        this.introduce = introduce;
        this.address = address;
        this.count = 0L;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
