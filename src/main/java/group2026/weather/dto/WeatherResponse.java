package group2026.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(
        CurrentWeather current,
        DailyWeather daily
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeather(
            Double temperature_2m,
            Double relative_humidity_2m,
            Double wind_speed_10m,
            Integer weather_code
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyWeather(
            List<String> time,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min,
            List<Integer> weather_code,
            List<Double> precipitation_probability_max
    ) {
    }
}
