package org.example.backend.foodpick.domain.like.model;

import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RestaurantLikeId implements Serializable {
    private Long user;
    private Long restaurant;
}
