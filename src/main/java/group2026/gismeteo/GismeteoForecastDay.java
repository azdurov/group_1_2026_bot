package group2026.gismeteo;

public record GismeteoForecastDay(
        String date,
        Integer minTemperature,
        Integer maxTemperature,
        Integer humidity,
        Double windSpeed,
        String description,
        Double precipitation
) {
}