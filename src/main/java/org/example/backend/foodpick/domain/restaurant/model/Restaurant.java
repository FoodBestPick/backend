package org.example.backend.foodpick.domain.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.tag.model.TagBridge;
import org.example.backend.foodpick.domain.food.model.FoodBridge;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "\"RESTAURANT\"")
public class Restaurant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANT_ID")
    private Long id;

    @Column(name = "RESTAURANT_NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "RESTAURANT_INTRODUCE", length = 500)
    private String introduce;

    @Column(name = "RESTAURANT_ADDRESS", nullable = true, length = 255)
    private String address;

    @Column(name = "RESTAURANT_COUNT", nullable = false)
    private Long count;

    @Column(name = "RESTAURANT_LATITUDE", length = 20)
    private String latitude;

    @Column(name = "RESTAURANT_LONGITUDE", length = 20)
    private String longitude;

    @Column(name = "RESTAURANT_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "RESTAURANT_UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantMenu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantPicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantTime> times = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TagBridge> tagBridges = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FoodBridge> foodBridges = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (count == null) count = 0L;
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ✅ 업데이트 메서드
    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void updateIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public void updateAddress(String address) {
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
    }

    public void updateLatitude(String latitude) {
        if (latitude != null && !latitude.isBlank()) {
            this.latitude = latitude;
        }
    }

    public void updateLongitude(String longitude) {
        if (longitude != null && !longitude.isBlank()) {
            this.longitude = longitude;
        }
    }
}