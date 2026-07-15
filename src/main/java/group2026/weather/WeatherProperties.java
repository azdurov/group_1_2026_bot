package group2026.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.location")
public record WeatherProperties(
        double latitude,
        double longitude
) {
}