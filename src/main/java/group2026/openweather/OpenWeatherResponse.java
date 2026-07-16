package group2026.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        List<Item> list
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            long dt,
            Main main,
            List<Weather> weather,
            Wind wind,
            double pop,
            Rain rain,
            Snow snow
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(
            double temp,
            double feels_like,
            double temp_min,
            double temp_max,
            int humidity
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(
            String main,
            String description,
            String icon
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(
            double speed
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rain(
            @JsonProperty("3h")
            double threeHour
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snow(
            @JsonProperty("3h")
            double threeHour
    ) {
    }
}