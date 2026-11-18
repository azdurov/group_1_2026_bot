package group2026.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public <T> T get(String url, Class<T> type) {
        return get(url, type, Map.of());
    }

    public <T> T get(String url, Class<T> type, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.forEach(builder::header);

        return send(builder.GET().build(), type);
    }

    public <T> T post(String url, Object body, Class<T> type) {
        return post(url, body, type, Map.of());
    }

    public <T> T post(String url, Object body, Class<T> type, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.forEach(builder::header);

        return send(builder.POST(HttpRequest.BodyPublishers.ofString(serialize(body))).build(), type);
    }

    private <T> T send(HttpRequest request, Class<T> type) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiRequestException("HTTP-запрос был прерван", e);
        } catch (IOException e) {
            throw new ApiRequestException("Не удалось выполнить HTTP-запрос", e);
        }

        if (response.statusCode() / 100 != 2) {
            throw new ApiRequestException("Ошибка HTTP: " + response.statusCode(), response.statusCode());
        }

        if (type == String.class) {
            return (T) response.body();
        }

        try {
            return objectMapper.readValue(response.body(), type);
        } catch (IOException e) {
            throw new ApiRequestException("Не удалось разобрать ответ", e);
        }
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ApiRequestException("Не удалось сериализовать запрос", e);
        }
    }
}