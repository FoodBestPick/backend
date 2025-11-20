package org.example.backend.foodpick.domain.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"RESTAURANT_TIME\"")
public class RestaurantTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTAURANT_TIME_ID")
    private Long id;

    @Column(name = "RESTAURANT_TIME_WEEK", length = 5, nullable = false)
    private String week;

    @Column(name = "RESTAURANT_STARTTIME", length = 10, nullable = false)
    private String startTime;

    @Column(name = "RESTAURANT_ENDTIME", length = 10, nullable = false)
    private String endTime;

    @Column(name = "RESTAURANT_RESTTIME", length = 25, nullable = false)
    private String restTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID", nullable = false)
    private Restaurant restaurant;

    @Column(name = "RESTAURANT_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "RESTAURANT_UPDATED_DATE", nullable = false)
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