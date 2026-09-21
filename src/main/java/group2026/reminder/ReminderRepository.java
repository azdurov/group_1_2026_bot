package group2026.reminder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.reminder.model.Reminder;
import group2026.upstash.UpstashRedisClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderRepository {

    private static final String REDIS_KEY = "reminders";
    private static final TypeReference<List<Reminder>> LIST_TYPE = new TypeReference<>() {
    };

    private final UpstashRedisClient redis;
    private final ObjectMapper objectMapper;

    private volatile List<Reminder> cache = List.of();

    @PostConstruct
    void init() {
        cache = loadFromRedis();
    }

    private List<Reminder> loadFromRedis() {
        String json = redis.get(REDIS_KEY);
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return List.copyOf(objectMapper.readValue(json, LIST_TYPE));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось прочитать напоминания из Redis", e);
        }
    }

    public List<Reminder> findFromCache() {
        return cache;
    }

    public List<Reminder> findFromRedis() {
        cache = loadFromRedis();
        return cache;
    }

    public void saveAll(List<Reminder> reminders) {
        List<Reminder> snapshot = List.copyOf(reminders);

        String value;
        try {
            value = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось сериализовать напоминания", e);
        }

        redis.set("reminders", value);
        cache = snapshot;
    }

    public boolean deleteById(String id) {
        List<Reminder> reminders = new ArrayList<>(cache);
        boolean removed = reminders.removeIf(r -> r.id().equals(id));
        if (removed) {
            saveAll(reminders);
        }
        return removed;
    }
}