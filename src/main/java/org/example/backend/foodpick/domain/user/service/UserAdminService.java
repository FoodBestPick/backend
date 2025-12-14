package org.example.backend.foodpick.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.model.AlarmTargetType;
import org.example.backend.foodpick.domain.alarm.model.AlarmType;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantSearchRepository;
import org.example.backend.foodpick.domain.review.repository.ReviewQueryRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.domain.review.repository.ReviewRepository;
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
import org.example.backend.foodpick.infra.websocket.WebSocketSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final UserQueryRepository userQueryRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantSearchRepository restaurantSearchRepository;
    private final RedisDashboardService redisDashboardService;
    private final AlarmService alarmService;
    private final WebSocketSessionManager webSocketSessionManager;

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
            if (banEndAt.equals(LocalDateTime.MAX)) {
                throw new CustomException(ErrorException.PERMANENTLY_BANNED);
            }

            user.updateStatus(UserStatus.SUSPENDED, banEndAt);
            webSocketSessionManager.forceLogout(user.getId());
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

        webSocketSessionManager.forceLogout(user.getId());

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 정지 처리가 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> unSuspendUser(String token, Long userId) {

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new CustomException(ErrorException.INVALID_USER_STATUS);
        }

        user.updateStatus(UserStatus.ACTIVED, null);
        user.updateMessage(null);

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 정지가 해제되었습니다.", null));
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

        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59, 999_999_999);

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
        LocalDateTime weekEndDateTime = weekEnd.atTime(23, 59, 59, 999_999_999);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime monthStartDateTime = monthStart.atStartOfDay();
        LocalDateTime monthEndDateTime = monthEnd.atTime(23, 59, 59, 999_999_999);


        List<Integer> weekTimeSeries = redisDashboardService.getTimeSeries(weekStart, weekEnd);

        List<Integer> allUserDataList =
                redisDashboardService.getWeeklyUserCounts(weekStart, weekEnd);

        int[] allUserData =
                allUserDataList.stream().mapToInt(Integer::intValue).toArray();
        int[] weekUserData = weekTimeSeries.stream().mapToInt(Integer::intValue).toArray();

        long totalRestaurants = restaurantRepository.count();

        long totalTodayReviews = reviewRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long totalWeekReviews = reviewRepository.countByCreatedAtBetween(weekStartDateTime, weekEndDateTime);
        long totalMonthReviews = reviewRepository.countByCreatedAtBetween(monthStartDateTime, monthEndDateTime);

        List<Integer> barDataList = redisDashboardService.getWeeklyReviewCounts(weekStart, weekEnd);
        int[] barData = barDataList.stream().mapToInt(Integer::intValue).toArray();

        List<Integer> restaurantBarDataList =
                redisDashboardService.getWeeklyRestaurantCounts(weekStart, weekEnd);

        int[] allRestaurantData =
                restaurantBarDataList.stream().mapToInt(Integer::intValue).toArray();

        List<PieItem> pieDataList = restaurantRepository.countRestaurantsByCategory();

        long total = pieDataList.stream()
                .mapToLong(PieItem::getPopulation)
                .sum();

        List<PieItem> percentPieDataList = pieDataList.stream()
                .map(item -> {
                    long percent = (total == 0)
                            ? 0
                            : (item.getPopulation() * 100) / total;

                    return PieItem.builder()
                            .name(item.getName())
                            .population(percent)
                            .build();
                })
                .toList();
        PieItem[] pieData = percentPieDataList.toArray(new PieItem[0]);

        UserDashboardResponse response = UserDashboardResponse.create(
                totalUsers,
                (int) totalRestaurants,
                (int) totalTodayReviews,
                (int) totalWeekReviews,
                (int) totalMonthReviews,
                allUserData,
                allRestaurantData,
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

        StatsPeriodDetail todayStats = buildTodayStats(today);
        StatsPeriodDetail weekStats = buildWeekStats(today);
        StatsPeriodDetail monthStats = buildMonthStats(today);
        StatsPeriodDetail customStats = buildCustomStats(startDate, endDate);

        UserStatsDetailResponse response = UserStatsDetailResponse.of(
                todayStats,
                weekStats,
                monthStats,
                customStats
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "유저 통계 조회 성공", response));
    }

    private StatsPeriodDetail buildTodayStats(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        long visitors = redisDashboardService.getTodayVisitors();
        long prevVisitors = redisDashboardService.getCustomVisitors(yesterday, yesterday);

        long joins = userRepository.countByCreatedAtBetween(
                today.atStartOfDay(), today.atTime(23,59,59));
        long prevJoins = userRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(), yesterday.atTime(23,59,59));

        long restaurants = restaurantRepository.countByCreatedDateBetween(
                today.atStartOfDay(), today.atTime(23,59,59));
        long prevRestaurants = restaurantRepository.countByCreatedDateBetween(
                yesterday.atStartOfDay(), yesterday.atTime(23,59,59));

        long reviews = reviewRepository.countByCreatedAtBetween(
                today.atStartOfDay(), today.atTime(23,59,59));
        long prevReviews = reviewRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(), yesterday.atTime(23,59,59));

        List<Integer> series = redisDashboardService.getTimeSeries(today, today);

        Map<String, Integer> categories = buildCategoryPercentMap(today.atStartOfDay(), today.atTime(23,59,59));

        List<Integer> ratingDistribution =
                buildRatingDistribution(
                        today.atStartOfDay(),
                        today.atTime(23,59,59)
                );

        List<TopSearch> topSearches =
                buildTopSearches(today.atStartOfDay(), today.atTime(23,59,59));

        return StatsPeriodDetail.ofUserStats(
                visitors, prevVisitors,
                joins, prevJoins,
                restaurants, prevRestaurants,
                reviews, prevReviews,
                series,
                categories, ratingDistribution, topSearches
        );
    }

    private StatsPeriodDetail buildWeekStats(LocalDate today) {
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        LocalDate prevWeekStart = weekStart.minusWeeks(1);
        LocalDate prevWeekEnd = weekEnd.minusWeeks(1);

        long visitors = redisDashboardService.getWeekVisitors();
        long prevVisitors = redisDashboardService.getCustomVisitors(prevWeekStart, prevWeekEnd);

        long joins = userRepository.countByCreatedAtBetween(
                weekStart.atStartOfDay(), weekEnd.atTime(23,59,59));
        long prevJoins = userRepository.countByCreatedAtBetween(
                prevWeekStart.atStartOfDay(), prevWeekEnd.atTime(23,59,59));

        long restaurants = restaurantRepository.countByCreatedDateBetween(
                weekStart.atStartOfDay(), weekEnd.atTime(23,59,59));
        long prevRestaurants = restaurantRepository.countByCreatedDateBetween(
                prevWeekStart.atStartOfDay(), prevWeekEnd.atTime(23,59,59));

        long reviews = reviewRepository.countByCreatedAtBetween(
                weekStart.atStartOfDay(), weekEnd.atTime(23,59,59));
        long prevReviews = reviewRepository.countByCreatedAtBetween(
                prevWeekStart.atStartOfDay(), prevWeekEnd.atTime(23,59,59));

        List<Integer> series = redisDashboardService.getTimeSeries(weekStart, weekEnd);

        Map<String, Integer> categories =
                buildCategoryPercentMap(
                        weekStart.atStartOfDay(),
                        weekEnd.atTime(23,59,59)
                );

        List<Integer> ratingDistribution =
                buildRatingDistribution(
                        weekStart.atStartOfDay(),
                        weekEnd.atTime(23,59,59)
                );

        List<TopSearch> topSearches =
                buildTopSearches(weekStart.atStartOfDay(), weekEnd.atTime(23,59,59));

        return StatsPeriodDetail.ofUserStats(
                visitors, prevVisitors,
                joins, prevJoins,
                restaurants, prevRestaurants,
                reviews, prevReviews,
                series,
                categories, ratingDistribution, topSearches
        );
    }

    private StatsPeriodDetail buildMonthStats(LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());

        long visitors = redisDashboardService.getMonthVisitors();
        long prevVisitors = redisDashboardService.getCustomVisitors(prevMonthStart, prevMonthEnd);

        long joins = userRepository.countByCreatedAtBetween(
                monthStart.atStartOfDay(), monthEnd.atTime(23,59,59));
        long prevJoins = userRepository.countByCreatedAtBetween(
                prevMonthStart.atStartOfDay(), prevMonthEnd.atTime(23,59,59));

        long restaurants = restaurantRepository.countByCreatedDateBetween(
                monthStart.atStartOfDay(), monthEnd.atTime(23,59,59));
        long prevRestaurants = restaurantRepository.countByCreatedDateBetween(
                prevMonthStart.atStartOfDay(), prevMonthEnd.atTime(23,59,59));

        long reviews = reviewRepository.countByCreatedAtBetween(
                monthStart.atStartOfDay(), monthEnd.atTime(23,59,59));
        long prevReviews = reviewRepository.countByCreatedAtBetween(
                prevMonthStart.atStartOfDay(), prevMonthEnd.atTime(23,59,59));

        List<Integer> series = redisDashboardService.getTimeSeries(monthStart, monthEnd);

        Map<String, Integer> categories =
                buildCategoryPercentMap(
                        monthStart.atStartOfDay(),
                        monthEnd.atTime(23,59,59)
                );

        List<Integer> ratingDistribution =
                buildRatingDistribution(
                        monthStart.atStartOfDay(),
                        monthEnd.atTime(23,59,59)
                );

        List<TopSearch> topSearches =
                buildTopSearches(monthStart.atStartOfDay(), monthEnd.atTime(23,59,59));


        return StatsPeriodDetail.ofUserStats(
                visitors, prevVisitors,
                joins, prevJoins,
                restaurants, prevRestaurants,
                reviews, prevReviews,
                series,
                categories, ratingDistribution, topSearches
        );
    }

    private StatsPeriodDetail buildCustomStats(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            return StatsPeriodDetail.ofUserStats(
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0,
                    List.of(),
                    Map.of(),
                    List.of(),
                    List.of()
            );
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevStart = startDate.minusDays(days);
        LocalDate prevEnd = endDate.minusDays(days);

        long visitors = redisDashboardService.getCustomVisitors(startDate, endDate);
        long prevVisitors = redisDashboardService.getCustomVisitors(prevStart, prevEnd);

        long joins = userRepository.countByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23,59,59));
        long prevJoins = userRepository.countByCreatedAtBetween(
                prevStart.atStartOfDay(), prevEnd.atTime(23,59,59));

        long restaurants = restaurantRepository.countByCreatedDateBetween(
                startDate.atStartOfDay(), endDate.atTime(23,59,59));
        long prevRestaurants = restaurantRepository.countByCreatedDateBetween(
                prevStart.atStartOfDay(), prevEnd.atTime(23,59,59));

        long reviews = reviewRepository.countByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23,59,59));
        long prevReviews = reviewRepository.countByCreatedAtBetween(
                prevStart.atStartOfDay(), prevEnd.atTime(23,59,59));

        List<Integer> series = redisDashboardService.getTimeSeries(startDate, endDate);

        Map<String, Integer> categories =
                buildCategoryPercentMap(
                        startDate.atStartOfDay(),
                        endDate.atTime(23,59,59)
                );

        List<Integer> ratingDistribution =
                buildRatingDistribution(
                        startDate.atStartOfDay(),
                        endDate.atTime(23,59,59)
                );

        List<TopSearch> topSearches =
                buildTopSearches(startDate.atStartOfDay(), endDate.atTime(23,59,59));


        return StatsPeriodDetail.ofUserStats(
                visitors, prevVisitors,
                joins, prevJoins,
                restaurants, prevRestaurants,
                reviews, prevReviews,
                series,
                categories, ratingDistribution, topSearches
        );
    }

    private Map<String, Integer> buildCategoryPercentMap(
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<PieItem> rawList =
                restaurantRepository.countRestaurantsByCategoryBetween(start, end);

        long total = rawList.stream()
                .mapToLong(PieItem::getPopulation)
                .sum();

        if (total == 0) return Map.of();

        Map<String, Integer> percentMap = rawList.stream()
                .collect(Collectors.toMap(
                        PieItem::getName,
                        item -> (int) ((item.getPopulation() * 100) / total)
                ));

        int sum = percentMap.values().stream().mapToInt(Integer::intValue).sum();
        if (sum != 100 && !percentMap.isEmpty()) {
            String key = percentMap.keySet().iterator().next();
            percentMap.put(key, percentMap.get(key) + (100 - sum));
        }

        return percentMap;
    }

    private List<Integer> buildRatingDistribution(
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<Object[]> raw =
                reviewRepository.countRatingDistributionBetween(start, end);

        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            map.put(i, 0);
        }

        for (Object[] row : raw) {
            int rating = ((Number) row[0]).intValue();
            int count  = ((Number) row[1]).intValue();
            map.put(rating, count);
        }

        return List.of(
                map.get(1),
                map.get(2),
                map.get(3),
                map.get(4),
                map.get(5)
        );
    }

    private List<TopSearch> buildTopSearches(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return restaurantSearchRepository.findTopKeywordsBetween(start, end)
                .stream()
                .limit(5)
                .map(row -> new TopSearch(
                        (String) row[0],
                        ((Number) row[1]).intValue()
                ))
                .toList();
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
