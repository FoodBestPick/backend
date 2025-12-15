package org.example.backend.foodpick.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.domain.chat.repository.ChatMessageRepository;
import org.example.backend.foodpick.domain.report.dto.MyReportResponse;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.repository.ReportRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AlarmService alarmService;
    private final ChatMessageRepository chatRepository;

    public ResponseEntity<ApiResponse<String>> sendReport(String token, SendReportRequest request){

        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity reporter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));


        switch (request.getTargetType()) {

            case USER:
                if (!userRepository.existsById(request.getTargetId())) {
                    throw new CustomException(ErrorException.USER_NOT_FOUND);
                }
                break;

                /*
            case REVIEW:
                if (!reviewRepository.existsById(request.getTargetId())) {
                    throw new CustomException(ErrorException.TARGET_NOT_FOUND);
                }
                break;
                 */
            case RESTAURANT:
                if (!restaurantRepository.existsById(request.getTargetId())) {
                    throw new CustomException(ErrorException.RESTAURANT_NOT_FOUND);
                }
                break;

            case CHAT_MESSAGE:
                if (!chatRepository.existsById(request.getTargetId())) {
                    throw new CustomException(ErrorException.CHAT_MESSAGE_NOT_FOUND);
                }
                break;
        }

        ReportEntity report = ReportEntity.create(reporter, request);
        reportRepository.save(report);

        List<UserEntity> adminList = userRepository.findByRole(UserRole.ADMIN);

        String message = reporter.getNickname() + "님이 새로운 신고를 접수했습니다.";

        for (UserEntity admin : adminList) {
            SendAlarmRequest alarmRequest = new SendAlarmRequest(
                    admin.getId(),
                    AlarmType.REPORT_RECEIVED,
                    AlarmTargetType.REPORT,
                    request.getTargetId(),
                    message
            );

            alarmService.sendAlarm(reporter.getId(), alarmRequest);
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "신고가 접수되었습니다.", null));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<MyReportResponse>>> getMyReport(String token){

        Long userId = jwtTokenValidator.getUserId(token);

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        List<ReportEntity> reports =
                reportRepository.findByReporterIdOrderByCreatedAtDesc(userId);

        List<MyReportResponse> response = reports.stream()
                .map(MyReportResponse::from)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "신고 내역을 가져왔습니다.", response));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteMyReport(String token, Long reportId) {

        Long userId = jwtTokenValidator.getUserId(token);

        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorException.REPORT_NOT_FOUND));

        if (!report.getReporter().getId().equals(userId)) {
            throw new CustomException(ErrorException.INVALID_REPORT);
        }

        reportRepository.delete(report);

        return ResponseEntity.ok(new ApiResponse<>(200, "신고 내역이 삭제되었습니다.", null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteAllMyReports(String token) {

        Long userId = jwtTokenValidator.getUserId(token);

        long count = reportRepository.countByReporterId(userId);
        if (count == 0) {
            return ResponseEntity.ok(new ApiResponse<>(200, "삭제할 신고 내역이 없습니다.", null));
        }

        reportRepository.deleteAllByReporterId(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "내 신고 내역이 모두 삭제되었습니다.", null));
    }
}
