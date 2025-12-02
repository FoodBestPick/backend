package org.example.backend.foodpick.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.report.dto.SendReportRequest;
import org.example.backend.foodpick.domain.report.model.ReportEntity;
import org.example.backend.foodpick.domain.report.repository.ReportRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
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
    private final RestaurantRepository restaurantRepository;

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
                    throw new CustomException(ErrorException.USER_NOT_FOUND);
                }
                break;

                /*
            case CHAT:
                if (!chatRepository.existsById(request.getTargetId())) {
                    throw new CustomException(ErrorException.TARGET_NOT_FOUND);
                }
                break;

                */
        }

        // 2. 신고 생성 및 저장
        ReportEntity report = ReportEntity.create(reporter, request);
        reportRepository.save(report);

        return ResponseEntity.ok(new ApiResponse<>(200, "신고가 접수되었습니다.", null));
    }


}
