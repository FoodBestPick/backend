package org.example.backend.foodpick.domain.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"RESTAURANT_PICTURE\"")
public class RestaurantPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANT_PICTURE_ID")
    private Long id;

    @Column(name = "RESTAURANT_PICTURE_URL", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @Column(name = "RESTAURANT_PICTURE_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "RESTAURANT_PICTURE_UPDATED_DATE", nullable = false)
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