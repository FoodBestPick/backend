package org.example.backend.foodpick.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.model.ReportStatus;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MyReportResponse {
    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private String reasonDetail;
    private ReportStatus status;
    private Boolean isImposed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MyReportResponse from(ReportEntity e) {
        return MyReportResponse.builder()
                .id(e.getId())
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .reason(e.getReason())
                .reasonDetail(e.getReasonDetail())
                .status(e.getStatus())
                .isImposed(e.getIsImposed())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
