package group2026.openweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.weather.WeatherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenWeatherProperties properties;
    private final WeatherProperties weatherProperties;

    public OpenWeatherResponse getWeather() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl()))
                .header(HttpHeaders.ACCEPT, "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("OpenWeather status: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), OpenWeatherResponse.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenWeather request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse OpenWeather response", e);
        }
    }

    private String buildUrl() {
        return "%s?lat=%s&lon=%s&units=metric&lang=ru&appid=%s".formatted(
                properties.apiUrl(),
                weatherProperties.latitude(),
                weatherProperties.longitude(),
                properties.apiKey()
        );
    }
}
