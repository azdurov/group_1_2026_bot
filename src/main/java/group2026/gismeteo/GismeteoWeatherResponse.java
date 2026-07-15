package group2026.gismeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GismeteoWeatherResponse(
        Meta meta,
        Data data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            int status_code,
            boolean status
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            City city,
            List<ForecastItem> forecast
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record City(
            String name,
            double latitude,
            double longitude
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastItem(
            String kind,
            DateTime date,
            Integer temperature,
            Integer humidity,
            Double windSpeed,
            String description,
            Precipitation precipitation
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateTime(
            String local,
            String UTC
    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Precipitation(
            Double amount,
            Integer type
    ) {
    }
}