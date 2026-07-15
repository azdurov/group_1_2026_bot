package group2026.gismeteo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gismeteo.weather")
public record GismeteoWeatherProperties(
        String apiKey,
        String apiUrl
) {
}