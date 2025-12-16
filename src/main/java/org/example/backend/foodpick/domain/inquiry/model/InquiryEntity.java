// domain/inquiry/model/InquiryEntity.java
package org.example.backend.foodpick.domain.inquiry.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.foodpick.domain.user.model.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inquiry")
public class InquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryCategory category;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String adminContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @ElementCollection
    @CollectionTable(name = "inquiry_image", joinColumns = @JoinColumn(name = "inquiry_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.updatedAt = LocalDateTime.now().withNano(0);
        if (this.status == null) this.status = InquiryStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public void answer(String adminContent) {
        this.adminContent = adminContent;
        this.status = InquiryStatus.ANSWERED;
    }

    public static InquiryEntity create(UserEntity user, InquiryCategory category, String title, String content, List<String> imageUrls) {
        return InquiryEntity.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .adminContent(null)
                .status(InquiryStatus.PENDING)
                .images(imageUrls == null ? List.of() : imageUrls)
                .build();
    }
}
