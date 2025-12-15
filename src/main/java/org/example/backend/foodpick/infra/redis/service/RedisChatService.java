package org.example.backend.foodpick.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisChatService {

    private final StringRedisTemplate redisTemplate;

    private static final String GEO_KEY = "match:geo";
    private static final String USER_QUEUE_KEY = "match:user:queue";

    public String buildQueueKey(String category, int count) {
        return "match:queue:" + category + ":" + count;
    }

    public boolean isMatching(Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(USER_QUEUE_KEY + ":" + userId)
        );
    }

    public void saveUserQueue(Long userId, String queueKey) {
        redisTemplate.opsForValue()
                .set(USER_QUEUE_KEY + ":" + userId, queueKey);
    }

    public String getUserQueue(Long userId) {
        return redisTemplate.opsForValue()
                .get(USER_QUEUE_KEY + ":" + userId);
    }

    public void deleteUserQueue(Long userId) {
        redisTemplate.delete(USER_QUEUE_KEY + ":" + userId);
    }

    public void enqueueUser(String queueKey, Long userId) {

        String userKey = USER_QUEUE_KEY + ":" + userId;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(userKey, queueKey);
        if (!Boolean.TRUE.equals(ok)) {
            throw new CustomException(ErrorException.ALREADY_MATCHING);
        }

        redisTemplate.opsForList().remove(queueKey, 0, userId.toString());

        redisTemplate.opsForList().rightPush(queueKey, userId.toString());

    }

    public void removeUserFromQueue(String queueKey, Long userId) {
        redisTemplate.opsForList().remove(queueKey, 0, userId.toString());
    }

    public void saveLocation(Long userId, double lat, double lng) {
        redisTemplate.opsForGeo()
                .add(GEO_KEY, new Point(lng, lat), userId.toString());
    }

    public void deleteLocation(Long userId) {
        redisTemplate.opsForGeo()
                .remove(GEO_KEY, userId.toString());
    }

    public Set<Long> findUsersInRadius(double lat, double lng, double radiusKm) {

        var results = redisTemplate.opsForGeo().radius(
                GEO_KEY,
                new Circle(new Point(lng, lat),
                        new Distance(radiusKm, Metrics.KILOMETERS))
        );

        if (results == null) return Set.of();

        return results.getContent().stream()
                .map(r -> Long.parseLong(r.getContent().getName()))
                .collect(Collectors.toSet());
    }

    public List<Long> matchFromQueue(
            String queueKey,
            Set<Long> nearUsers,
            int targetCount
    ) {

        List<String> candidates =
                redisTemplate.opsForList().range(queueKey, 0, -1);

        if (candidates == null || candidates.isEmpty()) return null;

        List<Long> matched = new ArrayList<>();

        for (String uidStr : candidates) {
            Long uid = Long.parseLong(uidStr);

            if (nearUsers.contains(uid)) {
                matched.add(uid);
            }

            if (matched.size() == targetCount) break;
        }

        if (matched.size() < targetCount) return null;

        //  큐 + GEO + 상태 전부 제거
        for (Long uid : matched) {
            removeUserFromQueue(queueKey, uid);
            deleteUserQueue(uid);
            deleteLocation(uid);
        }

        return matched;
    }

    public void cancelMatching(Long userId) {
        Set<String> keys = redisTemplate.keys("match:queue:*");
        if (keys != null) {
            for (String k : keys) {
                redisTemplate.opsForList().remove(k, 0, userId.toString());
            }
        }

        deleteLocation(userId);
        deleteUserQueue(userId);
    }
}
