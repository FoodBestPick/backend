package org.example.backend.foodpick.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.ReportListResponse;
import org.example.backend.foodpick.domain.report.dto.ReportPageResponse;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.model.ReportStatus;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;
import org.example.backend.foodpick.domain.report.repository.ReportRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportAdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;

    public ResponseEntity<ApiResponse<ReportPageResponse>> getAllReports(
            String token,
            int page,
            int size,
            String status,          // ← 필터링
            String targetType       // ← 필터링
    ) {
        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ReportEntity> reportPage;

        ReportStatus statusEnum = null;
        ReportTargetType typeEnum = null;

        if (status != null && !status.isBlank()) {
            statusEnum = ReportStatus.valueOf(status.toUpperCase());
        }

        if (targetType != null && !targetType.isBlank()) {
            typeEnum = ReportTargetType.valueOf(targetType.toUpperCase());
        }

        if (statusEnum != null && typeEnum != null) {
            reportPage = reportRepository.findByStatusAndTargetType(statusEnum, typeEnum, pageable);
        } else if (statusEnum != null) {
            reportPage = reportRepository.findByStatus(statusEnum, pageable);
        } else if (typeEnum != null) {
            reportPage = reportRepository.findByTargetType(typeEnum, pageable);
        } else {
            reportPage = reportRepository.findAll(pageable);
        }

        ReportPageResponse responseData = ReportPageResponse.from(reportPage);

        return ResponseEntity.ok(new ApiResponse<>(200, "신고 전체목록 조회 성공", responseData));
    }
}
