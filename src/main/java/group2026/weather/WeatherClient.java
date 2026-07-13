package group2026.weather;

import group2026.weather.dto.WeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class WeatherClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String weatherApiUrl;
    private final double latitude;
    private final double longitude;

    public WeatherClient(
            @Value("${weather.api.url}") String weatherApiUrl,
            @Value("${weather.api.latitude}") double latitude,
            @Value("${weather.api.longitude}") double longitude
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.weatherApiUrl = weatherApiUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public WeatherResponse getCurrentWeather() {
        String url = String.format(
                "%s/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max&timezone=Europe%%2FMinsk",
                weatherApiUrl, latitude, longitude
        );

        log.debug("Fetching weather data from: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), WeatherResponse.class);
            } else {
                log.error("Failed to fetch weather data. Status code: {}", response.statusCode());
                throw new RuntimeException("Failed to fetch weather data: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching weather data", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error fetching weather data", e);
        }
    }
}
