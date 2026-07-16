package group2026.openweather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.open-weather")
public record OpenWeatherProperties(
        String apiKey,
        String apiUrl
) {
}