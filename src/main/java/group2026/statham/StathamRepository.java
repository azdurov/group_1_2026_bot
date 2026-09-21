package group2026.statham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.upstash.UpstashRedisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StathamRepository {

    private static final String REDIS_KEY = "statham";

    private final UpstashRedisClient redis;
    private final ObjectMapper objectMapper;

    public StathamQuote getRandomQuote() {
        String json = redis.getRandom(REDIS_KEY);

        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, StathamQuote.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось прочитать цитату Стэтхема из Redis", e);
        }
    }
}