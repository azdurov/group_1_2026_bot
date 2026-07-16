package group2026.yandex;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather.yandex")
public record YandexWeatherProperties(
        String apiKey,
        String apiUrl
) {
}