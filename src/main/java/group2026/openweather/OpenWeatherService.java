package group2026.openweather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM (E)", LOCALE);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final OpenWeatherClient client;

    public String getForecast() {
        List<OpenWeatherForecastItem> forecast = client.getWeather().list().stream()
                .map(this::mapToItem)
                .toList();

        LocalDate today = LocalDate.now(ZONE_ID);

        Map<LocalDate, List<OpenWeatherForecastItem>> grouped = forecast.stream()
                .collect(Collectors.groupingBy(
                        item -> item.dateTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<OpenWeatherForecastItem> todayForecast = grouped.remove(today);
        if (todayForecast == null)
            todayForecast = List.of();

        StringBuilder result = new StringBuilder(512);

        result.append("🌤 Погода OpenWeather\n\n");

        appendToday(result, todayForecast);

        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(4)
                .forEach(entry -> appendDaySummary(result, entry.getKey(), entry.getValue()));

        return result.toString();
    }

    private void appendToday(StringBuilder result, List<OpenWeatherForecastItem> forecast) {
        if (forecast.isEmpty()) {
            return;
        }

        result.append("Сегодня ")
                .append(forecast.getFirst().dateTime().format(DATE_FORMAT))
                .append("\n");

        for (OpenWeatherForecastItem item : forecast) {

            result.append(item.dateTime().format(TIME_FORMAT))
                    .append("  ")
                    .append(getWeatherIcon(item.condition()))
                    .append(' ')
                    .append(formatTemperature(item.temperature()))
                    .append("  💨")
                    .append(formatWind(item.windSpeed()))
                    .append("м/с");

            appendPrecipitation(result, item.precipitationProbability(), item.precipitationMm());

            result.append('\n');
        }

        result.append('\n');
    }

    private void appendDaySummary(StringBuilder result, LocalDate date, List<OpenWeatherForecastItem> items) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double windSum = 0;
        double precipitation = 0;
        double maxProbability = 0;

        OpenWeatherForecastItem representative = items.getFirst();

        for (OpenWeatherForecastItem item : items) {

            if (item.temperature() < min)
                min = item.temperature();

            if (item.temperature() > max) {
                max = item.temperature();
                representative = item;
            }

            windSum += item.windSpeed();
            precipitation += item.precipitationMm();
            maxProbability = Math.max(maxProbability, item.precipitationProbability());
        }

        result.append(date.format(DATE_FORMAT))
                .append("  ")
                .append(getWeatherIcon(representative.condition()))
                .append(' ')
                .append(representative.description())
                .append('\n');

        result.append(formatTemperature(min))
                .append("...")
                .append(formatTemperature(max))
                .append("  💨")
                .append(formatWind(windSum / items.size()))
                .append("м/с");

        appendPrecipitation(result, maxProbability, precipitation);

        result.append("\n\n");
    }

    private OpenWeatherForecastItem mapToItem(OpenWeatherResponse.Item item) {
        OpenWeatherResponse.Weather weather = item.weather().isEmpty()
                ? new OpenWeatherResponse.Weather("", "нет данных", "")
                : item.weather().getFirst();

        return new OpenWeatherForecastItem(
                Instant.ofEpochSecond(item.dt()).atZone(ZONE_ID).toLocalDateTime(),
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

    private double getPrecipitation(OpenWeatherResponse.Item item) {
        double rain = item.rain() == null ? 0 : item.rain().threeHour();
        double snow = item.snow() == null ? 0 : item.snow().threeHour();
        return rain + snow;
    }

    private void appendPrecipitation(StringBuilder result, double probability, double millimeters) {
        int percent = Math.round((float) (probability * 100));
        if (percent == 0)
            return;

        result.append("  🌧")
                .append(percent)
                .append("% ")
                .append(String.format(LOCALE, "%.1f", millimeters))
                .append("мм");
    }

    private String formatTemperature(double value) {
        long temp = Math.round(value);
        return (temp > 0 ? "+" : "") + temp + "°";
    }

    private String formatWind(double value) {
        return String.format(LOCALE, "%.1f", value);
    }

    private String getWeatherIcon(String condition) {
        return switch (condition.toLowerCase(Locale.ROOT)) {
            case "clear" -> "☀️";
            case "clouds" -> "☁️";
            case "rain" -> "🌧";
            case "drizzle" -> "🌦";
            case "snow" -> "❄️";
            case "thunderstorm" -> "⛈";
            case "mist", "fog", "haze" -> "🌫";
            default -> "";
        };
    }
}
