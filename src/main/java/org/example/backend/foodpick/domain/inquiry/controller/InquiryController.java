package org.example.backend.foodpick.domain.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryCreateRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.service.InquiryService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "문의(사용자)", description = "사용자 문의 등록/조회/삭제 API")
@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(
            summary = "문의 등록",
            description = "multipart/form-data로 `data(JSON)` + `file(이미지들)`을 함께 전송합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InquiryListResponse>> createInquiry(
            @Parameter(
                    name = "data",
                    description = "문의 내용(JSON)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InquiryCreateRequest.class)
                    )
            )
            @RequestPart(value = "data") InquiryCreateRequest request,

            @Parameter(
                    name = "file",
                    description = "첨부 이미지(선택, 여러 개 가능)",
                    required = false,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart(value = "file", required = false) List<MultipartFile> files,

            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.createInquiry(request, files, token);
    }

    @Operation(summary = "내 문의 목록 조회", description = "로그인한 사용자의 문의 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getMyInquiries(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.getMyInquiries(token);
    }

    @Operation(summary = "문의 단건 삭제", description = "내 문의를 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문의 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete/{inquiry_id}")
    public ResponseEntity<ApiResponse<String>> deleteInquiry(
            @Parameter(description = "문의 ID", example = "1")
            @PathVariable("inquiry_id") Long inquiryId,

            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.deleteInquiry(inquiryId, token);
    }

    @Operation(summary = "내 문의 전체 삭제", description = "로그인한 사용자의 문의를 전체 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllInquirys(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.deleteAllInquirys(token);
    }
}
