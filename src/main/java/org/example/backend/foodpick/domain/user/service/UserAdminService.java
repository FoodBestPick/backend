package org.example.backend.foodpick.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.domain.user.dto.*;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserStatus;
import org.example.backend.foodpick.domain.user.repository.UserQueryRepository;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.redis.service.RedisDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final UserQueryRepository userQueryRepository;
    private final RedisDashboardService redisDashboardService;
    private final AlarmService alarmService;

    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserAll(String token){
        Long myId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(myId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        List<UserEntity> users = userRepository.findAll();

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::of)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "모든 유저들의 데이터입니다.", userResponses));
    }

    public ResponseEntity<ApiResponse<String>> warningUpdate(
            String token,
            Long userId,
            WarningUpdateReqeust request
    ) {

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        UserEntity user = userRepository.findById(userId)
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

        SendAlarmRequest alarmRequest = new SendAlarmRequest(
                user.getId(),
                AlarmType.WARNING_ADDED,
                AlarmTargetType.USER,
                user.getId(),
                "경고 " + addWarning + "회가 부여되었습니다. (총 " + newWarning + "회)"
        );

        alarmService.sendAlarm(adminId, alarmRequest);

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 경고 누적 및 제재 처리가 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> userSuspende(String token, Long userId, SuspendeRequest request) {

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        UserEntity user = userRepository.findById(userId)
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

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 정지 처리가 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> userRoleUpdate(String token, Long userId, UserRoleRequest request){

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        UserRole newRole = request.getRole();

        if (newRole == null) {
            throw new CustomException(ErrorException.INVALID_ROLE);
        }

        user.updateRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 역할이 변경되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<UserDashboardResponse>> getDashboard(String token) {

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        long totalUsers = userQueryRepository.countAllUsers();
        List<Long> allUserDataList = userQueryRepository.findAllUserData();

        // 주간 방문 통계 (Monday ~ Sunday)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        List<Integer> weekTimeSeries = redisDashboardService.getTimeSeries(weekStart, weekEnd);

        int[] allUserData = allUserDataList.stream().mapToInt(Long::intValue).toArray();
        int[] weekUserData = weekTimeSeries.stream().mapToInt(Integer::intValue).toArray();

        /* 이 아래 부분은 아직 맛집/리뷰 데이터가 없으므로 하드코딩 유지 */
        int restaurants = 1234;
        int todayReviews = 52;
        int weekReviews = 280;
        int monthReviews = 1128;

        int[] barData = new int[]{50, 60, 70, 65, 80, 90, 70};

        PieItem[] pieData = new PieItem[]{
                PieItem.builder().name("한식").population(40).build(),
                PieItem.builder().name("중식").population(20).build(),
                PieItem.builder().name("일식").population(15).build(),
                PieItem.builder().name("양식").population(10).build(),
                PieItem.builder().name("카페").population(15).build()
        };

        UserDashboardResponse response = UserDashboardResponse.create(
                totalUsers,
                restaurants,
                todayReviews,
                weekReviews,
                monthReviews,
                allUserData,
                weekUserData,
                barData,
                pieData
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "대시보드 조회 성공", response));
    }

    public ResponseEntity<ApiResponse<UserStatsDetailResponse>> getUserStats(
            String token,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        LocalDate today = LocalDate.now();

        long todayVisitors = redisDashboardService.getTodayVisitors();
        long todayJoins = userRepository.countByCreatedAtBetween(
                today.atStartOfDay(), today.atTime(23,59,59)
        );
        List<Integer> todaySeries = redisDashboardService.getTimeSeries(today, today);

        StatsPeriodDetail todayStats =
                StatsPeriodDetail.ofUserStats(todayVisitors, todayJoins, todaySeries);

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        long weekVisitors = redisDashboardService.getWeekVisitors();
        long weekJoins = userRepository.countByCreatedAtBetween(
                weekStart.atStartOfDay(), weekEnd.atTime(23,59,59)
        );
        List<Integer> weekSeries = redisDashboardService.getTimeSeries(weekStart, weekEnd);

        StatsPeriodDetail weekStats =
                StatsPeriodDetail.ofUserStats(weekVisitors, weekJoins, weekSeries);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long monthVisitors = redisDashboardService.getMonthVisitors();
        long monthJoins = userRepository.countByCreatedAtBetween(
                monthStart.atStartOfDay(), monthEnd.atTime(23,59,59)
        );
        List<Integer> monthSeries = redisDashboardService.getTimeSeries(monthStart, monthEnd);

        StatsPeriodDetail monthStats =
                StatsPeriodDetail.ofUserStats(monthVisitors, monthJoins, monthSeries);


        StatsPeriodDetail customStats;

        if (startDate != null && endDate != null) {
            long customVisitors = redisDashboardService.getCustomVisitors(startDate, endDate);
            long customJoins = userRepository.countByCreatedAtBetween(
                    startDate.atStartOfDay(), endDate.atTime(23,59,59)
            );
            List<Integer> customSeries = redisDashboardService.getTimeSeries(startDate, endDate);

            customStats = StatsPeriodDetail.ofUserStats(customVisitors, customJoins, customSeries);

        } else {
            // 기본값
            customStats = StatsPeriodDetail.ofUserStats(0, 0, List.of());
        }

        UserStatsDetailResponse response = UserStatsDetailResponse.of(
                todayStats,
                weekStats,
                monthStats,
                customStats
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "유저 통계 조회 성공", response));
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
