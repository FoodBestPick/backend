package org.example.backend.foodpick.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.ReportListResponse;
import org.example.backend.foodpick.domain.report.dto.ReportPageResponse;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.model.ReportStatus;
import org.example.backend.foodpick.domain.report.model.ReportTargetType;
import org.example.backend.foodpick.domain.report.repository.ReportRepository;
import org.example.backend.foodpick.domain.user.dto.SuspendeRequest;
import org.example.backend.foodpick.domain.user.dto.WarningUpdateReqeust;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserStatus;
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

import java.time.LocalDateTime;
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

    public ResponseEntity<ApiResponse<String>> deleteReport(String token, Long reportId){

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorException.REPORT_NOT_FOUND));

        reportRepository.delete(report);

        return ResponseEntity.ok(new ApiResponse<>(200, "신고 삭제가 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> deleteAllReports(String token){
        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        reportRepository.deleteAllInBatch();

        return ResponseEntity.ok(new ApiResponse<>(200, "신고 전체 삭제가 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> approveWithWarning(
            String token,
            Long reportId,
            WarningUpdateReqeust request
    ) {

        Long adminId = jwtTokenValidator.getUserId(token);
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorException.REPORT_NOT_FOUND));

        UserEntity user = userRepository.findById(report.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        int addWarning = request.getWarnings();

        if (addWarning < 1 || addWarning > 5) {
            throw new CustomException(ErrorException.INVALID_WARNING_RANGE);
        }

        int newWarning = user.getWarnings() + addWarning;
        String message = request.getMessage();
        user.updateWarning(newWarning, message);

        LocalDateTime banEndAt = calculateBanDuration(newWarning);

        if (banEndAt != null) {
            user.updateStatus(UserStatus.SUSPENDED, banEndAt);
        }

        userRepository.save(user);
        report.update(admin);
        reportRepository.save(report);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "신고 승인 및 경고 처리가 완료되었습니다.", null)
        );
    }

    public ResponseEntity<ApiResponse<String>> approveWithSuspension(
            String token,
            Long reportId,
            SuspendeRequest request
    ) {

        Long adminId = jwtTokenValidator.getUserId(token);
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorException.REPORT_NOT_FOUND));

        UserEntity user = userRepository.findById(report.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        int days = request.getDay();

        if (days <= 0) {
            throw new CustomException(ErrorException.INVALID_BAN_DURATION);
        }

        LocalDateTime banEndAt;

        if (days == 999) {
            banEndAt = LocalDateTime.MAX;
        } else {
            banEndAt = LocalDateTime.now().plusDays(days).withNano(0);
        }

        user.updateStatus(UserStatus.SUSPENDED, banEndAt);
        user.updateMessage(request.getMessage());
        userRepository.save(user);

        report.update(admin);
        reportRepository.save(report);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "신고 승인 및 정지 처리가 완료되었습니다.", null)
        );
    }


    private LocalDateTime calculateBanDuration(int warning) {
        switch (warning) {
            case 2:
                return LocalDateTime.now().plusDays(1);
            case 3:
                return LocalDateTime.now().plusDays(3);
            case 4:
                return LocalDateTime.now().plusDays(7);
            case 5:
                return LocalDateTime.now().plusDays(30);
            default:
                if (warning >= 6) {
                    return LocalDateTime.MAX;
                }
                return null;
        }
    }

}
