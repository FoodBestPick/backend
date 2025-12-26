package org.example.backend.foodpick.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.MyReportResponse;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.service.ReportService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "신고(사용자)", description = "사용자 신고 등록/조회/삭제 API")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 등록", description = "사용자가 대상(댓글/리뷰/유저 등)을 신고합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> sendReport(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "신고 등록 요청 DTO",
                    content = @Content(schema = @Schema(implementation = SendReportRequest.class))
            )
            @RequestBody SendReportRequest request
    ) {
        return reportService.sendReport(token, request);
    }

    @Operation(summary = "내 신고 목록 조회", description = "로그인한 사용자의 신고 내역을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MyReportResponse>>> getMyReport(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return reportService.getMyReport(token);
    }

    @Operation(summary = "내 신고 단건 삭제", description = "내 신고 내역 1건을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고 내역 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{report_id}/delete")
    public ResponseEntity<ApiResponse<String>> deleteMyReport(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "신고 ID", example = "1")
            @PathVariable("report_id") Long reportId
    ) {
        return reportService.deleteMyReport(token, reportId);
    }

    @Operation(summary = "내 신고 전체 삭제", description = "로그인한 사용자의 신고 내역을 전체 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(Authorization 누락/만료)")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteAllMyReports(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String token
    ) {
        return reportService.deleteAllMyReports(token);
    }
}
