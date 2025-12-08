package org.example.backend.foodpick.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.ReportPageResponse;
import org.example.backend.foodpick.domain.report.service.ReportAdminService;
import org.example.backend.foodpick.domain.user.dto.SuspendeRequest;
import org.example.backend.foodpick.domain.user.dto.WarningUpdateReqeust;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportAdminService reportAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReportPageResponse>> getAllReports(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "targetType", required = false) String targetType
    ) {
        return reportAdminService.getAllReports(token, page, size, status, targetType);
    }


    @DeleteMapping("/{report_id}/delete")
    public ResponseEntity<ApiResponse<String>> deleteReport(@RequestHeader("Authorization") String token,
                                                          @PathVariable("report_id") Long reportId ) {
        return reportAdminService.deleteReport(token, reportId);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteAllReports(@RequestHeader("Authorization") String token) {
        return reportAdminService.deleteAllReports(token);
    }

    @PatchMapping("/{report_id}/warning")
    public ResponseEntity<ApiResponse<String>> warningUpdate(@RequestHeader("Authorization") String token,
                                                             @PathVariable("report_id") Long reportId,
                                                             @RequestBody WarningUpdateReqeust request){
        return reportAdminService.approveWithWarning(token, reportId, request);
    }

    @PatchMapping("/{report_id}/suspende")
    public ResponseEntity<ApiResponse<String>> userSuspende(@RequestHeader("Authorization") String token,
                                                            @PathVariable("report_id") Long reportId,
                                                            @RequestBody SuspendeRequest request){
        return reportAdminService.approveWithSuspension(token, reportId, request);
    }
}
