package group2026.yandex;

public record YandexForecastDay(
        String date,
        Integer minTemperature,
        Integer maxTemperature,
        Integer temperature,
        Integer humidity,
        Double windSpeed,
        String condition
) {
}