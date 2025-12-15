package org.example.backend.foodpick.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.MyReportResponse;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.service.ReportService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> sendReport(@RequestHeader("Authorization") String token,
                                                          @RequestBody SendReportRequest request){
        return reportService.sendReport(token, request);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyReportResponse>>> getMyReport(@RequestHeader("Authorization") String token){
        return reportService.getMyReport(token);
    }

    @DeleteMapping("/{report_id}/delete")
    public ResponseEntity<ApiResponse<String>> deleteMyReport(
            @RequestHeader("Authorization") String token,
            @PathVariable("report_id") Long reportId
    ) {
        return reportService.deleteMyReport(token, reportId);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteAllMyReports(
            @RequestHeader("Authorization") String token
    ) {
        return reportService.deleteAllMyReports(token);
    }
}
