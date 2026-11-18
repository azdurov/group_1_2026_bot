package group2026.joke;

import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.http.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JokeClient {

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final JokeProperties properties;

    public JokeResponse getRandomJoke() {
        String body = apiClient.get(buildUrl(), String.class);

        if (body == null || body.isBlank()) {
            throw new RuntimeException("Пустой ответ от API анекдотов");
        }

        try {
            return objectMapper.readValue(cleanJson(body), JokeResponse.class);
        } catch (Exception e) {
            log.error("Cannot parse joke response: {}", body, e);
            throw new RuntimeException("Не удалось разобрать ответ", e);
        }
    }

    private String buildUrl() {
        return properties.apiUrl() + "?CType=" + properties.category();
    }

    private String cleanJson(String json) {
        return json
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "");
    }
}
