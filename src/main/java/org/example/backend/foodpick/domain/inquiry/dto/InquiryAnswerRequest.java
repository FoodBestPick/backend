package org.example.backend.foodpick.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문의 답변 요청(관리자)")
public class InquiryAnswerRequest {

    @Schema(description = "관리자 답변 내용", example = "확인 결과 토큰 만료로 보입니다. 다시 로그인해 주세요.")
    private String adminContent;
}
