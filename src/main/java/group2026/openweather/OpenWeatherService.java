package group2026.openweather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenWeatherService {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Minsk");

    private static final Locale LOCALE = Locale.forLanguageTag("ru");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM (E)", LOCALE);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");


    private final OpenWeatherClient client;


    public String formatForecast() {

        List<OpenWeatherForecastItem> forecast = getForecast();

        LocalDate today = LocalDate.now(ZONE_ID);


        List<OpenWeatherForecastItem> todayForecast =
                forecast.stream()
                        .filter(item ->
                                item.dateTime()
                                        .toLocalDate()
                                        .equals(today)
                        )
                        .toList();


        Map<LocalDate, List<OpenWeatherForecastItem>> otherDays =
                forecast.stream()
                        .filter(item ->
                                !item.dateTime()
                                        .toLocalDate()
                                        .equals(today)
                        )
                        .collect(Collectors.groupingBy(
                                item -> item.dateTime().toLocalDate()
                        ));


        StringBuilder result = new StringBuilder();

        result.append("🌤 Погода OpenWeather\n\n");


        appendToday(result, todayForecast);


        otherDays.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(4)
                .forEach(entry ->
                        appendDaySummary(
                                result,
                                entry.getKey(),
                                entry.getValue()
                        )
                );


        return result.toString();
    }


    private List<OpenWeatherForecastItem> getForecast() {

        OpenWeatherResponse response = client.getWeather();

        return response.list()
                .stream()
                .map(this::mapToItem)
                .toList();
    }


    private void appendToday(
            StringBuilder result,
            List<OpenWeatherForecastItem> forecast
    ) {

        if (forecast.isEmpty()) {
            return;
        }


        result.append("Сегодня ")
                .append(
                        forecast.getFirst()
                                .dateTime()
                                .format(DATE_FORMAT)
                )
                .append("\n\n");


        forecast.forEach(item -> {

            result.append(
                    item.dateTime()
                            .format(TIME_FORMAT)
            );


            result.append("  ")
                    .append(getWeatherIcon(item.condition()))
                    .append(" ")
                    .append(Math.round(item.temperature()))
                    .append("°C");


            result.append("  💨")
                    .append(formatWind(item.windSpeed()))
                    .append("м/с");


            appendPrecipitation(
                    result,
                    item.precipitationProbability(),
                    item.precipitationMm()
            );


            result.append("\n");
        });


        result.append("\n");
    }


    private void appendDaySummary(
            StringBuilder result,
            LocalDate date,
            List<OpenWeatherForecastItem> items
    ) {

        double min =
                items.stream()
                        .mapToDouble(OpenWeatherForecastItem::temperature)
                        .min()
                        .orElse(0);


        double max =
                items.stream()
                        .mapToDouble(OpenWeatherForecastItem::temperature)
                        .max()
                        .orElse(0);


        OpenWeatherForecastItem main =
                items.stream()
                        .max(
                                Comparator.comparingDouble(
                                        OpenWeatherForecastItem::temperature
                                )
                        )
                        .orElse(items.getFirst());


        double wind =
                items.stream()
                        .mapToDouble(OpenWeatherForecastItem::windSpeed)
                        .average()
                        .orElse(0);


        double precipitation =
                items.stream()
                        .mapToDouble(
                                OpenWeatherForecastItem::precipitationMm
                        )
                        .sum();


        int precipitationProbability =
                (int) (
                        items.stream()
                                .mapToDouble(
                                        OpenWeatherForecastItem::precipitationProbability
                                )
                                .max()
                                .orElse(0)
                                * 100
                );


        result.append(date.format(DATE_FORMAT))
                .append("  ")
                .append(getWeatherIcon(main.condition()))
                .append(" ")
                .append(main.description())
                .append("\n");


        result.append(Math.round(min))
                .append("...+")
                .append(Math.round(max))
                .append("°C");


        result.append("  💨")
                .append(formatWind(wind))
                .append("м/с");


        appendPrecipitation(
                result,
                precipitationProbability / 100.0,
                precipitation
        );


        result.append("\n\n");
    }


    private void appendPrecipitation(
            StringBuilder result,
            double probability,
            double millimeters
    ) {

        int percent = (int) (probability * 100);


        result.append("  🌧")
                .append(percent)
                .append("%");


        result.append(" ")
                .append(
                        String.format(
                                LOCALE,
                                "%.1f",
                                millimeters
                        )
                )
                .append("мм");
    }


    private OpenWeatherForecastItem mapToItem(
            OpenWeatherResponse.Item item
    ) {

        OpenWeatherResponse.Weather weather =
                item.weather().isEmpty()
                        ? new OpenWeatherResponse.Weather(
                        "",
                        "нет данных",
                        ""
                )
                        : item.weather().getFirst();


        return new OpenWeatherForecastItem(
                Instant.ofEpochSecond(item.dt())
                        .atZone(ZONE_ID)
                        .toLocalDateTime(),

                item.main().temp(),

                item.main().feels_like(),

                item.wind().speed(),

                item.pop(),

                getPrecipitation(item),

                weather.main(),

                weather.description(),

                weather.icon()
        );
    }


    private double getPrecipitation(
            OpenWeatherResponse.Item item
    ) {

        double rain =
                item.rain() == null
                        ? 0
                        : item.rain().threeHour();


        double snow =
                item.snow() == null
                        ? 0
                        : item.snow().threeHour();


        return rain + snow;
    }


    private String formatWind(double value) {

        return String.format(
                LOCALE,
                "%.1f",
                value
        );
    }


    private String getWeatherIcon(String condition) {

        return switch (condition.toLowerCase()) {

            case "clear" -> "☀️";

            case "clouds" -> "☁️";

            case "rain" -> "🌧";

            case "drizzle" -> "🌦";

            case "snow" -> "❄️";

            case "thunderstorm" -> "⛈";

            case "mist", "fog", "haze" -> "🌫";

            default -> "🌍";
        };
    }
}