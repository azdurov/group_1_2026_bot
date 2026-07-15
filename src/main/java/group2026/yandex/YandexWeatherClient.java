package group2026.yandex;

import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.weather.WeatherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexWeatherClient {

    private static final int FORECAST_DAYS = 4;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final YandexWeatherProperties properties;
    private final WeatherProperties weatherProperties;

    public YandexWeatherResponse getWeather() {
        validateConfiguration();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl()))
                .header("X-Yandex-API-Key", properties.apiKey())
                .header(HttpHeaders.ACCEPT, "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Yandex Weather API returned status: " + response.statusCode()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    YandexWeatherResponse.class
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);

        } catch (IOException e) {
            throw new RuntimeException("Cannot read Yandex Weather response", e);
        }
    }

    private String buildUrl() {
        return "%s?lat=%s&lon=%s&lang=%s&limit=%d&hours=false"
                .formatted(
                        properties.apiUrl(),
                        weatherProperties.latitude(),
                        weatherProperties.longitude(),
                        URLEncoder.encode("ru_RU", StandardCharsets.UTF_8),
                        FORECAST_DAYS
                );
    }

    private void validateConfiguration() {

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new RuntimeException("Yandex API key is not configured");
        }

        if (properties.apiUrl() == null || properties.apiUrl().isBlank()) {
            throw new RuntimeException("Yandex API url is not configured");
        }
    }

}