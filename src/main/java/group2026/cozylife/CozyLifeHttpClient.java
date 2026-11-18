package group2026.cozylife;

import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.http.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CozyLifeHttpClient {

    private static final String USER_AGENT = "DoHomeX/1.20.4 (iPhone; iOS 17.3.1; Scale/3.00)";

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final CozyLifeProperties properties;

    public <T> T get(String path, Map<String, List<String>> query, Class<T> type) {
        return apiClient.get(buildUrl(path, query), type, headers());
    }

    public <T> T post(String path, Object body, Class<T> type) {
        String raw = apiClient.post(properties.apiUrl() + path, body, String.class, headers());
        log.debug("CozyLife POST {} -> {}", path, raw);

        try {
            return objectMapper.readValue(raw, type);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось разобрать ответ CozyLife", e);
        }
    }

    private Map<String, String> headers() {
        return Map.of(
                HttpHeaders.USER_AGENT, USER_AGENT,
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
        );
    }

    private String buildUrl(String path, Map<String, List<String>> query) {
        StringBuilder url = new StringBuilder(properties.apiUrl()).append(path);

        if (query != null && !query.isEmpty()) {
            url.append('?');
            query.forEach((key, values) -> {
                if (values == null) {
                    return;
                }
                for (String value : values) {
                    url.append(key).append('=').append(encode(value)).append('&');
                }
            });
            url.setLength(url.length() - 1);
        }

        return url.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}