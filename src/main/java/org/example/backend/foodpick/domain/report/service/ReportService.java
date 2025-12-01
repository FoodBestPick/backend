package org.example.backend.foodpick.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.repository.ReportRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final UserRepository userRepository;

    public ResponseEntity<ApiResponse<String>> sendReport(String token, SendReportRequest request){

        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity reporter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        ReportEntity report = ReportEntity.create(reporter, request);
        reportRepository.save(report);

        return ResponseEntity.ok(new ApiResponse<>(200, "신고가 접수되었습니다.", null));
    }

}
