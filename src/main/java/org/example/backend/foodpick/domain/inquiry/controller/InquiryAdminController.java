package org.example.backend.foodpick.domain.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryAnswerRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;
import org.example.backend.foodpick.domain.inquiry.service.InquiryAdminService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "문의(관리자)", description = "관리자 문의 답변/조회 API")
@RestController
@RequestMapping("admin/inquiry")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;

    @Operation(summary = "문의 답변 등록/수정", description = "관리자가 문의에 답변을 등록/수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답변 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문의 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{inquiry_id}/answer")
    public ResponseEntity<ApiResponse<String>> answerInquiry(
            @Parameter(description = "문의 ID", example = "1")
            @PathVariable("inquiry_id") Long inquiryId,

            @RequestBody InquiryAnswerRequest request,

            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return inquiryAdminService.answerInquiry(inquiryId, request, token);
    }

    @Operation(summary = "문의 목록 조회(관리자)", description = "관리자가 문의 목록을 조회합니다. status로 필터링 가능.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getInquiries(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "문의 상태 필터(선택)", example = "WAITING")
            @RequestParam(required = false) InquiryStatus status
    ) {
        return inquiryAdminService.getInquiries(token, status);
    }
}
