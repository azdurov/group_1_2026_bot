package group2026.openweather;

import java.time.LocalDateTime;

public record OpenWeatherForecastItem(
        LocalDateTime dateTime,
        double temperature,
        double feelsLike,
        double windSpeed,
        double precipitationProbability,
        double precipitationMm,
        String condition,
        String description,
        String icon
) {
}