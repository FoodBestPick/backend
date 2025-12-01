package org.example.backend.foodpick.domain.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.model.ReportStatus;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportListResponse {

    private Long id;
    private String reason;
    private String reasonDetail;
    private ReportTargetType targetType;
    private Long targetId;
    private Long reporterId;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public static ReportListResponse from(ReportEntity e) {
        return ReportListResponse.builder()
                .id(e.getId())
                .reason(e.getReason())
                .reasonDetail(e.getReasonDetail())
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .reporterId(e.getReporter().getId())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

