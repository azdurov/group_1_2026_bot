package group2026.yandex;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yandex.weather")
public record YandexWeatherProperties(
        String apiKey,
        String apiUrl
) {
}