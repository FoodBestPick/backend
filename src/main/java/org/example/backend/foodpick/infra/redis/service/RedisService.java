package org.example.backend.foodpick.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void recordLogin(Long userId, UserRole role) {
        if (UserRole.ADMIN.equals(role)) return;

        LocalDate today = LocalDate.now();
        int year = today.get(IsoFields.WEEK_BASED_YEAR);
        int weekOfYear = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

        String key = "week:user:" + year + "-W" + weekOfYear;

        redisTemplate.opsForSet().add(key, today + ":" + userId);

        int dayValue = today.getDayOfWeek().getValue();
        long ttlDays = 7 - dayValue + 1;

        redisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
    }

    public void recordVisit(Long userId, UserRole role) {
        if (UserRole.ADMIN.equals(role)) return;

        LocalDate today = LocalDate.now();
        int hour = LocalTime.now().getHour();

        String hourKey = "visitors:hour:" + today + ":" + hour;
        redisTemplate.opsForSet().add(hourKey, userId.toString());
        redisTemplate.expire(hourKey, 1, TimeUnit.DAYS);

        // ---- Day ----
        String dayKey = "visitors:day:" + today;
        redisTemplate.opsForSet().add(dayKey, userId.toString());
        redisTemplate.expire(dayKey, 1, TimeUnit.DAYS);

        // ---- Week ----
        int year = today.get(IsoFields.WEEK_BASED_YEAR);
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String weekKey = "visitors:week:" + year + "-W" + week;

        redisTemplate.opsForSet().add(weekKey, userId.toString());
        int dayOfWeek = today.getDayOfWeek().getValue();
        long ttlWeek = 7 - dayOfWeek + 1;
        redisTemplate.expire(weekKey, ttlWeek, TimeUnit.DAYS);

        // ---- Month ----
        String monthKey = "visitors:month:" + today.getYear() + "-" + today.getMonthValue();
        redisTemplate.opsForSet().add(monthKey, userId.toString());

        int lastDay = today.lengthOfMonth();
        long ttlMonth = lastDay - today.getDayOfMonth() + 1;
        redisTemplate.expire(monthKey, ttlMonth, TimeUnit.DAYS);
    }


    /* ------------------------ 방문자 카운트 (기본) ------------------------ */

    public long getTodayVisitors() {
        String key = "visitors:day:" + LocalDate.now();
        Set<String> set = redisTemplate.opsForSet().members(key);
        return set != null ? set.size() : 0;
    }

    public long getWeekVisitors() {
        LocalDate today = LocalDate.now();
        int year = today.get(IsoFields.WEEK_BASED_YEAR);
        int week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String key = "visitors:week:" + year + "-W" + week;

        Set<String> set = redisTemplate.opsForSet().members(key);
        return set != null ? set.size() : 0;
    }

    public long getMonthVisitors() {
        LocalDate today = LocalDate.now();
        String key = "visitors:month:" + today.getYear() + "-" + today.getMonthValue();
        Set<String> set = redisTemplate.opsForSet().members(key);
        return set != null ? set.size() : 0;
    }


    /* ------------------------ 커스텀 방문자 수 ------------------------ */

    public long getCustomVisitors(LocalDate startDate, LocalDate endDate) {
        Set<String> uniqueUsers = new HashSet<>();
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            String key = "visitors:day:" + date;
            Set<String> dayUsers = redisTemplate.opsForSet().members(key);
            if (dayUsers != null) uniqueUsers.addAll(dayUsers);
            date = date.plusDays(1);
        }

        return uniqueUsers.size();
    }

    public List<Integer> getTimeSeries(LocalDate startDate, LocalDate endDate) {

        long diffDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;

        // 1일: 4개 블록
        if (diffDays == 1) {
            return getTodayBlocks(startDate);
        }

        // ≤7일: daily
        if (diffDays <= 7) {
            return getDailyVisitors(startDate, endDate);
        }

        if (isMonthRange(startDate, endDate)) {
            return getMonthlyVisitors(startDate, endDate);
        }

        // ≤30일: weekly
        if (diffDays <= 30) {
            return getWeeklyVisitors(startDate, endDate);
        }

        // ≤90일: bi-weekly
        if (diffDays <= 90) {
            return getBiWeeklyVisitors(startDate, endDate);
        }

        // ≤180일: monthly
        if (diffDays <= 180) {
            return getMonthlyVisitors(startDate, endDate);
        }

        // ≤365일: bi-monthly
        if (diffDays <= 365) {
            return getBiMonthlyVisitors(startDate, endDate);
        }

        return getYearlyVisitors(startDate, endDate);
    }


    /* ---- 1일: 4개 구간 ---- */
    private List<Integer> getTodayBlocks(LocalDate date) {

        int morning = 0;   // 00~06
        int noon = 0;      // 06~12
        int afternoon = 0; // 12~18
        int evening = 0;   // 18~24

        for (int hour = 0; hour < 24; hour++) {

            String key = "visitors:hour:" + date + ":" + hour;
            Set<String> users = redisTemplate.opsForSet().members(key);
            int count = users != null ? users.size() : 0;

            if (hour < 6) morning += count;
            else if (hour < 12) noon += count;
            else if (hour < 18) afternoon += count;
            else evening += count;
        }

        return List.of(morning, noon, afternoon, evening);
    }

    /* ---- Daily ---- */
    private List<Integer> getDailyVisitors(LocalDate start, LocalDate end) {
        List<Integer> result = new ArrayList<>();
        LocalDate d = start;

        while (!d.isAfter(end)) {
            String key = "visitors:day:" + d;
            Set<String> users = redisTemplate.opsForSet().members(key);
            result.add(users != null ? users.size() : 0);
            d = d.plusDays(1);
        }

        return result;
    }

    /* ---- Weekly ---- */
    private List<Integer> getWeeklyVisitors(LocalDate start, LocalDate end) {
        List<Integer> weekly = new ArrayList<>();

        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            LocalDate weekEnd = cursor.plusDays(6);
            if (weekEnd.isAfter(end)) weekEnd = end;

            int sum = getDailyVisitors(cursor, weekEnd)
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            weekly.add(sum);

            cursor = weekEnd.plusDays(1);
        }

        return weekly;
    }

    /* ---- Monthly ---- */
    private List<Integer> getMonthlyVisitors(LocalDate start, LocalDate end) {

        // 1) monthStart, monthEnd 계산
        LocalDate monthStart = start.withDayOfMonth(1);
        LocalDate monthEnd = start.withDayOfMonth(start.lengthOfMonth());

        List<Integer> result = new ArrayList<>();

        // 2) 첫 주의 시작(월요일) 계산
        LocalDate firstWeekStart = monthStart.with(DayOfWeek.MONDAY);
        if (firstWeekStart.isAfter(monthStart)) {
            firstWeekStart = firstWeekStart.minusWeeks(1);
        }

        // 3) 커서 시작
        LocalDate cursor = firstWeekStart;

        while (!cursor.isAfter(monthEnd)) {
            LocalDate weekStart = cursor;
            LocalDate weekEnd = cursor.plusDays(6);

            // 이번 달 범위를 넘으면 조절
            if (weekStart.isBefore(monthStart)) weekStart = monthStart;
            if (weekEnd.isAfter(monthEnd)) weekEnd = monthEnd;

            // 방문자 합계 계산
            int sum = getDailyVisitors(weekStart, weekEnd)
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            result.add(sum);

            cursor = cursor.plusWeeks(1); // 다음 주
        }

        return result;
    }

    private List<Integer> getBiWeeklyVisitors(LocalDate start, LocalDate end) {
        List<Integer> result = new ArrayList<>();
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            LocalDate blockEnd = cursor.plusDays(13); // 14일 단위
            if (blockEnd.isAfter(end)) blockEnd = end;

            int sum = getDailyVisitors(cursor, blockEnd)
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            result.add(sum);

            cursor = blockEnd.plusDays(1);
        }

        return result;
    }

    private List<Integer> getBiMonthlyVisitors(LocalDate start, LocalDate end) {
        Map<YearMonth, Integer> monthMap = new LinkedHashMap<>();

        LocalDate d = start;

        while (!d.isAfter(end)) {
            YearMonth ym = YearMonth.from(d);

            String key = "visitors:day:" + d;
            Set<String> users = redisTemplate.opsForSet().members(key);
            int count = (users == null) ? 0 : users.size();

            monthMap.put(ym, monthMap.getOrDefault(ym, 0) + count);

            d = d.plusDays(1);
        }

        // 이제 2개월씩 묶는다
        List<Integer> result = new ArrayList<>();
        List<Integer> monthValues = new ArrayList<>(monthMap.values());

        for (int i = 0; i < monthValues.size(); i += 2) {
            int sum = monthValues.get(i);
            if (i + 1 < monthValues.size()) {
                sum += monthValues.get(i + 1);
            }
            result.add(sum);
        }

        return result;
    }

    private List<Integer> getYearlyVisitors(LocalDate start, LocalDate end) {
        Map<Integer, Integer> yearMap = new LinkedHashMap<>();

        LocalDate d = start;

        while (!d.isAfter(end)) {
            int year = d.getYear();

            String key = "visitors:day:" + d;
            Set<String> users = redisTemplate.opsForSet().members(key);
            int count = (users == null) ? 0 : users.size();

            yearMap.put(year, yearMap.getOrDefault(year, 0) + count);

            d = d.plusDays(1);
        }

        return new ArrayList<>(yearMap.values());
    }

    private boolean isMonthRange(LocalDate start, LocalDate end) {
        return start.getDayOfMonth() == 1 && end.getDayOfMonth() == end.lengthOfMonth();
    }
}