package org.example.backend.foodpick.domain.favorite.model;

import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FavoriteId implements Serializable {
    private Long user;
    private Long restaurant;
}