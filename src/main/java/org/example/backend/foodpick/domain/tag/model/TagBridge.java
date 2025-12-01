package org.example.backend.foodpick.domain.tag.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"TAG_BRIDGE\"")
public class TagBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ✅ DB 스키마: TAG_BRIDGE (ID 아님)
    @Column(name = "TAG_BRIDGE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID", nullable = false)
    private Tag tag;
}