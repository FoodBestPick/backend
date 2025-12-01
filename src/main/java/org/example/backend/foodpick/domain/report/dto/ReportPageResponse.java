package org.example.backend.foodpick.domain.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ReportPageResponse {

    private List<ReportListResponse> reports;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;

    public static ReportPageResponse from(Page<ReportEntity> page) {
        List<ReportListResponse> reports = page.getContent().stream()
                .map(ReportListResponse::from)
                .toList();

        return ReportPageResponse.builder()
                .reports(reports)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
