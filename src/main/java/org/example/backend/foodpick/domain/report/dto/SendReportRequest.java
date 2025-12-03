package org.example.backend.foodpick.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendReportRequest {

    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private String reasonDetail;

}
