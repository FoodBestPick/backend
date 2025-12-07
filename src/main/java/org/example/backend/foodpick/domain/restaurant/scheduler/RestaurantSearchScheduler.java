package org.example.backend.foodpick.domain.restaurant.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.model.RestaurantSearchEntity;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantSearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RestaurantSearchScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final RestaurantSearchRepository restaurantSearchRepository;

    private static final String PREFIX = "search:restaurant:";

    @Scheduled(cron = "0 5 0 * * *")
    public void saveYesterdaySearchStat() {

        LocalDate targetDate = LocalDate.now().minusDays(1);
        String key = PREFIX + targetDate;

        Set<ZSetOperations.TypedTuple<String>> data =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        if (data == null || data.isEmpty()) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> item : data) {
            RestaurantSearchEntity entity = RestaurantSearchEntity.builder()
                    .keyword(item.getValue())
                    .count(item.getScore().longValue())
                    .createdAt(targetDate.atStartOfDay())
                    .build();

            restaurantSearchRepository.save(entity);
        }

        redisTemplate.delete(key);
    }
}