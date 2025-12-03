package org.example.backend.foodpick.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.service.ReportService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
