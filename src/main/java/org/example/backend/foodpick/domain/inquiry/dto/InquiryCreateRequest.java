package org.example.backend.foodpick.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.model.InquiryCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 생성 요청")
public class InquiryCreateRequest {

    @Schema(description = "문의 카테고리", example = "ACCOUNT")
    private InquiryCategory category;

    @Schema(description = "문의 제목", example = "로그인이 안돼요")
    private String title;

    @Schema(description = "문의 내용", example = "로그인 시 401이 발생합니다. 확인 부탁드립니다.")
    private String content;
}
