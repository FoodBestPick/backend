package org.example.backend.foodpick.domain.review.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.user.model.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @Column(name = "REVIEW_CONTENT", columnDefinition = "text")
    private String content;

    @Column(name = "REVIEW_RATING", nullable = false)
    private Double rating;

    @ElementCollection
    @CollectionTable(name = "review_image", joinColumns = @JoinColumn(name = "REVIEW_ID"))
    @Column(name = "IMAGE_URL")
    private List<String> images = new ArrayList<>();

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String content, Double rating, List<String> images) {
        this.content = content;
        this.rating = rating;
        if (images != null) {
            this.images = images;
        }
    }
}
