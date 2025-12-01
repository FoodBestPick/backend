package org.example.backend.foodpick.domain.tag.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "\"TAG\"")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TAG_ID")
    private Long id;

    @Column(name = "TAG_NAME", nullable = false, unique = true)
    private String name;

    // ✅ 카테고리 추가
    @Column(name = "TAG_CATEGORY", length = 20)
    private String category; // PURPOSE, FACILITY, ATMOSPHERE

    @Column(name = "TAG_CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "TAG_UPDATED_DATE", nullable = false)
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

    // 간단한 업데이트 메서드
    public void updateName(String name) {
        if (name != null && !name.isBlank()) this.name = name;
    }

    // ✅ 카테고리 업데이트 메서드 추가
    public void updateCategory(String category) {
        this.category = category;
    }
}