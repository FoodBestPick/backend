package org.example.backend.foodpick.domain.like.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.user.model.UserEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(RestaurantLikeId.class)
@Table(name = "RESTAURANT_LIKE")
public class RestaurantLike {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID")
    private Restaurant restaurant;
}
