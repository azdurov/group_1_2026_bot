package group2026.openweather;

import group2026.http.ApiClient;
import group2026.weather.WeatherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    private final ApiClient apiClient;
    private final OpenWeatherProperties properties;
    private final WeatherProperties weatherProperties;

    public OpenWeatherResponse getWeather() {
        return apiClient.get(buildUrl(), OpenWeatherResponse.class);
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
