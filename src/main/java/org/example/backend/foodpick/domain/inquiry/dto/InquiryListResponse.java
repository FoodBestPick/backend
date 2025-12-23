package org.example.backend.foodpick.domain.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.inquiry.model.InquiryCategory;
import org.example.backend.foodpick.domain.inquiry.model.InquiryEntity;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "문의 목록/상세 응답")
public class InquiryListResponse {

    @Schema(description = "문의 ID", example = "1")
    private Long id;

    @Schema(description = "문의 카테고리", example = "ACCOUNT")
    private InquiryCategory category;

    @Schema(description = "문의 제목", example = "로그인이 안돼요")
    private String title;

    @Schema(description = "사용자 문의 내용", example = "로그인 시 401이 발생합니다.")
    private String userContent;

    @Schema(description = "관리자 답변 내용", example = "토큰 만료로 보입니다. 재로그인 해주세요.", nullable = true)
    private String adminContent;

    @Schema(description = "첨부 이미지 URL 목록", example = "[\"https://.../a.png\", \"https://.../b.png\"]")
    private List<String> images;

    @Schema(description = "문의 상태", example = "WAITING")
    private InquiryStatus status;

    @Schema(description = "생성일시", example = "2025-12-23 21:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-12-23 21:35:00")
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
