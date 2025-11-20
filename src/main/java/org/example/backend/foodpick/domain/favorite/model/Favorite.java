package org.example.backend.foodpick.domain.favorite.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(FavoriteId.class)
@Table(name = "\"FAVORITE\"")
public class Favorite {

    @Id
    @Column(name = "USER_ID")
    private Long user; // ✅ 추가 (복합키)

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @Column(name = "FAVORITE_ID", insertable = false, updatable = false)
    private Long id;

    @Column(name = "FAVORITE_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }
}