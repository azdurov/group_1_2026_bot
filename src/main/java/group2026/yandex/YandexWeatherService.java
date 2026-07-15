package group2026.yandex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexWeatherService {

    private final YandexWeatherClient client;

    public List<YandexForecastDay> getForecast() {

        YandexWeatherResponse response = client.getWeather();

        return Arrays.stream(response.forecasts())
                .map(this::toForecastDay)
                .toList();
    }

    public List<YandexForecastDay> getMorningForecast() {
        return getForecast()
                .stream()
                .limit(3)
                .toList();
    }

    public List<YandexForecastDay> getEveningForecast() {
        return getForecast()
                .stream()
                .skip(1)
                .limit(3)
                .toList();
    }

    public String formatForecast(List<YandexForecastDay> forecast) {

        StringBuilder sb = new StringBuilder();

        sb.append("🌤 Яндекс\n\n");

        for (YandexForecastDay day : forecast) {

            sb.append("📅 ")
                    .append(day.date())
                    .append('\n');

            sb.append("🌡 ")
                    .append(day.minTemperature())
                    .append("...+")
                    .append(day.maxTemperature())
                    .append("°C\n");

            sb.append("☁ ")
                    .append(condition(day.condition()))
                    .append('\n');

            sb.append("💨 ")
                    .append(day.windSpeed())
                    .append(" м/с\n");

            sb.append("💧 ")
                    .append(day.humidity())
                    .append("%\n\n");
        }

        return sb.toString();
    }

    private YandexForecastDay toForecastDay(YandexWeatherResponse.Forecast forecast) {

        var day = forecast.parts().day_short();

        return new YandexForecastDay(
                forecast.date(),
                day.temp_min(),
                day.temp_max(),
                day.temp(),
                day.humidity(),
                day.wind_speed(),
                day.condition()
        );
    }

    private String condition(String code) {

        return switch (code) {
            case "clear" -> "☀️ Ясно";
            case "partly-cloudy" -> "🌤 Малооблачно";
            case "cloudy" -> "☁ Облачно";
            case "overcast" -> "☁ Пасмурно";
            case "drizzle" -> "🌦 Морось";
            case "light-rain" -> "🌦 Небольшой дождь";
            case "rain" -> "🌧 Дождь";
            case "heavy-rain" -> "⛈ Сильный дождь";
            case "showers" -> "🌧 Ливень";
            case "wet-snow" -> "🌨 Мокрый снег";
            case "light-snow" -> "❄ Небольшой снег";
            case "snow" -> "❄ Снег";
            case "snow-showers" -> "🌨 Снегопад";
            case "hail" -> "🧊 Град";
            case "thunderstorm" -> "⛈ Гроза";
            case "thunderstorm-with-rain" -> "⛈🌧 Гроза с дождем";
            case "thunderstorm-with-hail" -> "⛈🧊 Гроза с градом";
            default -> "Неизвестно";
        };
    }
}