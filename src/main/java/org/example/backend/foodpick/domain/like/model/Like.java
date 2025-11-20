package org.example.backend.foodpick.domain.like.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(LikeId.class)
@Table(name = "\"LIKE\"")
public class Like {

    @Id
    @Column(name = "USER_ID")
    private Long user; // ✅ 추가 (복합키)

    @Id
    @Column(name = "RESTAURANT_REVIEW_ID")
    private Long restaurantReview; // ✅ 추가 (복합키)

    @Column(name = "LIKE_ID", insertable = false, updatable = false)
    private Long id;
}