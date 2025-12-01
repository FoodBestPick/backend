package org.example.backend.foodpick.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.ReportPageResponse;
import org.example.backend.foodpick.domain.report.service.ReportAdminService;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType
    ) {
        return reportAdminService.getAllReports(token, page, size, status, targetType);
    }
}
