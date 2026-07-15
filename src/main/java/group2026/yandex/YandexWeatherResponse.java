package group2026.yandex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YandexWeatherResponse(
        Fact fact,
        Forecast[] forecasts
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fact(
            double temp,
            double feels_like,
            int pressure_mm,
            int humidity,
            double wind_speed,
            String condition,
            String icon
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Forecast(
            String date,
            Parts parts
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parts(
            DayShort day_short
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DayShort(
            Integer temp,
            Integer temp_min,
            Integer temp_max,
            Integer humidity,
            Double wind_speed,
            String condition
    ) {
    }
}