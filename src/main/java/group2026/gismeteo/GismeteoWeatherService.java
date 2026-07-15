package group2026.gismeteo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GismeteoWeatherService {

    private final GismeteoWeatherClient client;

    public List<GismeteoForecastDay> getForecast() {

        var response = client.getWeather();

        Map<String, List<GismeteoWeatherResponse.ForecastItem>> days =
                response.data()
                        .forecast()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        item -> item.date().local().substring(0, 10)
                                )
                        );

        return days.entrySet()
                .stream()
                .map(this::toDay)
                .sorted()
                .toList();
    }

    public List<GismeteoForecastDay> getMorningForecast() {

        return getForecast()
                .stream()
                .limit(3)
                .toList();
    }

    public List<GismeteoForecastDay> getEveningForecast() {

        return getForecast()
                .stream()
                .skip(1)
                .limit(3)
                .toList();
    }

    private GismeteoForecastDay toDay(
            Map.Entry<String,
                    List<GismeteoWeatherResponse.ForecastItem>> entry
    ) {


        var items = entry.getValue();


        return new GismeteoForecastDay(
                entry.getKey(),

                items.stream()
                        .map(GismeteoWeatherResponse.ForecastItem::temperature)
                        .min(Integer::compare)
                        .orElse(0),

                items.stream()
                        .map(GismeteoWeatherResponse.ForecastItem::temperature)
                        .max(Integer::compare)
                        .orElse(0),

                items.stream()
                        .map(GismeteoWeatherResponse.ForecastItem::humidity)
                        .filter(x -> x != null)
                        .findFirst()
                        .orElse(0),

                items.stream()
                        .map(GismeteoWeatherResponse.ForecastItem::windSpeed)
                        .filter(x -> x != null)
                        .findFirst()
                        .orElse(0D),

                items.get(0).description(),

                items.stream()
                        .map(GismeteoWeatherResponse.ForecastItem::precipitation)
                        .filter(x -> x != null)
                        .map(GismeteoWeatherResponse.Precipitation::amount)
                        .filter(x -> x != null)
                        .findFirst()
                        .orElse(0D)
        );
    }
}