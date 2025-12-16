package org.example.backend.foodpick.domain.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.inquiry.model.InquiryCategory;
import org.example.backend.foodpick.domain.inquiry.model.InquiryEntity;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InquiryListResponse {
    private Long id;
    private InquiryCategory category;
    private String title;
    private String userContent;
    private String adminContent;
    private List<String> images;
    private InquiryStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static InquiryListResponse from(InquiryEntity e) {
        return InquiryListResponse.builder()
                .id(e.getId())
                .category(e.getCategory())
                .title(e.getTitle())
                .userContent(e.getContent())
                .adminContent(e.getAdminContent())
                .images(e.getImages())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
