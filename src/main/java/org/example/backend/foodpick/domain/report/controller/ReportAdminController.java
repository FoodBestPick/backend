package org.example.backend.foodpick.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.ReportPageResponse;
import org.example.backend.foodpick.domain.report.service.ReportAdminService;
import org.example.backend.foodpick.domain.user.dto.SuspendeRequest;
import org.example.backend.foodpick.domain.user.dto.WarningUpdateReqeust;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고(관리자)", description = "관리자 신고 관리(조회/삭제/제재) API")
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    @Operation(
            summary = "신고 목록 조회(관리자)",
            description = "관리자가 신고 목록을 페이징 조회합니다. status/targetType 필터링 가능."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<ReportPageResponse>> getAllReports(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,

            @Parameter(description = "상태 필터(선택)", example = "PENDING")
            @RequestParam(value = "status", required = false) String status,

            @Parameter(description = "대상 타입 필터(선택)", example = "COMMENT")
            @RequestParam(value = "targetType", required = false) String targetType
    ) {
        return reportAdminService.getAllReports(token, page, size, status, targetType);
    }

    @Operation(summary = "신고 단건 삭제(관리자)", description = "관리자가 신고 1건을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{report_id}/delete")
    public ResponseEntity<ApiResponse<String>> deleteReport(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "신고 ID", example = "1")
            @PathVariable("report_id") Long reportId
    ) {
        return reportAdminService.deleteReport(token, reportId);
    }

    @Operation(summary = "신고 전체 삭제(관리자)", description = "관리자가 신고 내역을 전체 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteAllReports(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return reportAdminService.deleteAllReports(token);
    }

    @Operation(
            summary = "신고 승인 + 경고 부여",
            description = "관리자가 신고를 승인하고, 사용자 경고를 부여합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{report_id}/warning")
    public ResponseEntity<ApiResponse<String>> warningUpdate(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "신고 ID", example = "1")
            @PathVariable("report_id") Long reportId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "경고 부여 요청 DTO",
                    content = @Content(schema = @Schema(implementation = WarningUpdateReqeust.class))
            )
            @RequestBody WarningUpdateReqeust request
    ) {
        return reportAdminService.approveWithWarning(token, reportId, request);
    }

    @Operation(
            summary = "신고 승인 + 정지(제재) 부여",
            description = "관리자가 신고를 승인하고, 사용자 계정을 정지 처리합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음(관리자만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{report_id}/suspende")
    public ResponseEntity<ApiResponse<String>> userSuspende(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "신고 ID", example = "1")
            @PathVariable("report_id") Long reportId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "정지(제재) 요청 DTO",
                    content = @Content(schema = @Schema(implementation = SuspendeRequest.class))
            )
            @RequestBody SuspendeRequest request
    ) {
        return reportAdminService.approveWithSuspension(token, reportId, request);
    }
}
