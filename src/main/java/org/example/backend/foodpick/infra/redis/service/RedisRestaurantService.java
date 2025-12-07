package org.example.backend.foodpick.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RedisRestaurantService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "search:restaurant:";

    public void increaseSearchCount(String keyword) {
        String key = PREFIX + LocalDate.now();
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1);
    }
}
