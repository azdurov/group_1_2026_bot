package group2026.upstash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpstashRedisClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(500);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String OK = "OK";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UpstashProperties properties;

    public String get(String key) {
        return execute("GET", key).result();
    }

    public String getRandom(String key) {
        return execute("SRANDMEMBER", key).result();
    }

    public void set(String key, String value) {
        UpstashResponse response = execute("SET", key, value);

        if (!OK.equals(response.result())) {
            throw new UpstashRedisException("Неожиданный результат SET: " + response.result());
        }
    }

    private UpstashResponse execute(String... command) {
        HttpRequest request = buildRequest(command);

        for (int attempt = 1; true; attempt++) {
            try {
                return send(request);
            } catch (IOException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw new UpstashRedisException("Ошибка запроса к Upstash", e);
                }

                long delay = RETRY_BACKOFF.toMillis() * (1L << (attempt - 1));
                log.warn("Upstash недоступен, попытка {}/{} через {}мс: {}",
                        attempt, MAX_ATTEMPTS, delay, e.getMessage());

                sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UpstashRedisException("Запрос к Upstash прерван", e);
            }
        }
    }

    private HttpRequest buildRequest(String... command) {
        try {
            return HttpRequest.newBuilder()
                    .uri(URI.create(properties.url()))
                    .timeout(REQUEST_TIMEOUT)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(command)))
                    .build();
        } catch (JsonProcessingException e) {
            throw new UpstashRedisException("Не удалось сериализовать команду Upstash", e);
        }
    }

    private UpstashResponse send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new UpstashRedisException("HTTP " + response.statusCode() + ": " + response.body());
        }

        UpstashResponse result;
        try {
            result = objectMapper.readValue(response.body(), UpstashResponse.class);
        } catch (JsonProcessingException e) {
            throw new UpstashRedisException("Не удалось десериализовать ответ Upstash", e);
        }

        if (result.error() != null) {
            throw new UpstashRedisException("Ошибка Redis: " + result.error());
        }

        return result;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpstashRedisException("Запрос к Upstash прерван", e);
        }
    }

    private record UpstashResponse(String result, String error) {
    }

    public static class UpstashRedisException extends RuntimeException {

        public UpstashRedisException(String message) {
            super(message);
        }

        public UpstashRedisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}