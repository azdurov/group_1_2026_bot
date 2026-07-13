package group2026.service;

import group2026.weather.WeatherClient;
import group2026.weather.dto.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final Locale RUSSIAN_LOCALE = new Locale("ru");

    private final WeatherClient weatherClient;
    private final MessageSource messageSource;

    public WeatherService(WeatherClient weatherClient, MessageSource messageSource) {
        this.weatherClient = weatherClient;
        this.messageSource = messageSource;
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, RUSSIAN_LOCALE);
    }

    private String getWeatherDescriptionText(Integer weatherCode) {
        if (weatherCode == null) {
            return getMessage("weather.unknown");
        }
        return switch (weatherCode) {
            case 0 -> getMessage("weather.code.clear");
            case 1, 2, 3 -> getMessage("weather.code.cloudy");
            case 45, 48 -> getMessage("weather.code.fog");
            case 51, 53, 55 -> getMessage("weather.code.drizzle");
            case 61, 63, 65 -> getMessage("weather.code.rain");
            case 71, 73, 75 -> getMessage("weather.code.snow");
            case 80, 81, 82 -> getMessage("weather.code.shower");
            case 95, 96, 99 -> getMessage("weather.code.thunderstorm");
            default -> getMessage("weather.unknown");
        };
    }

    public Optional<WeatherResponse> getWeather() {
        try {
            WeatherResponse response = weatherClient.getCurrentWeather();
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Error getting weather", e);
            return Optional.empty();
        }
    }

    public String formatWeatherMessage(WeatherResponse response) {
        if (response == null || response.current() == null) {
            return getMessage("message.error.data.not.available");
        }

        var current = response.current();
        String weatherDescription = getWeatherDescriptionText(current.weather_code());

        return String.format(
                "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s",
                getMessage("weather.current"),
                getMessage("weather.temperature", current.temperature_2m()),
                getMessage("weather.humidity", current.relative_humidity_2m()),
                getMessage("weather.wind", current.wind_speed_10m()),
                getMessage("weather.description", weatherDescription)
        );
    }

    public String formatMorningForecast(WeatherResponse response) {
        if (response == null || response.daily() == null) {
            return getMessage("message.error.forecast.not.available");
        }

        var daily = response.daily();
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage("weather.morning.forecast")).append("\n\n");

        int daysCount = Math.min(3, daily.time().size());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getMessage("weather.date.format"), RUSSIAN_LOCALE);

        for (int i = 0; i < daysCount; i++) {
            String date = daily.time().get(i);
            LocalDate localDate = LocalDate.parse(date);
            String dayName = i == 0 ? getMessage("weather.date.today") :
                    i == 1 ? getMessage("weather.date.tomorrow") :
                            localDate.format(dateFormatter);

            double tempMax = daily.temperature_2m_max().get(i);
            double tempMin = daily.temperature_2m_min().get(i);
            int weatherCode = daily.weather_code().get(i);
            double precipProb = daily.precipitation_probability_max().get(i);

            String weatherDesc = getWeatherDescriptionText(weatherCode);

            sb.append(String.format(
                    "📅 %s:\n" +
                            "%s\n" +
                            "%s\n" +
                            "📝 %s\n\n",
                    dayName,
                    getMessage("weather.max.temp", tempMax, tempMin),
                    getMessage("weather.precip.probability", precipProb),
                    weatherDesc
            ));
        }

        if (response.current() != null) {
            var current = response.current();
            sb.append(getMessage("weather.current.status",
                    current.temperature_2m(),
                    getWeatherDescriptionText(current.weather_code())));
        }

        return sb.toString();
    }

    public String formatEveningForecast(WeatherResponse response) {
        if (response == null || response.daily() == null) {
            return getMessage("message.error.forecast.not.available");
        }

        var daily = response.daily();
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage("weather.evening.forecast")).append("\n\n");

        int daysCount = Math.min(4, daily.time().size());
        int startIndex = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getMessage("weather.date.format"), RUSSIAN_LOCALE);

        for (int i = startIndex; i < daysCount; i++) {
            String date = daily.time().get(i);
            LocalDate localDate = LocalDate.parse(date);
            String dayName = i == 1 ? getMessage("weather.date.tomorrow") : localDate.format(dateFormatter);

            double tempMax = daily.temperature_2m_max().get(i);
            double tempMin = daily.temperature_2m_min().get(i);
            int weatherCode = daily.weather_code().get(i);
            double precipProb = daily.precipitation_probability_max().get(i);

            String weatherDesc = getWeatherDescriptionText(weatherCode);

            sb.append(String.format(
                    "📅 %s:\n" +
                            "%s\n" +
                            "%s\n" +
                            "📝 %s\n\n",
                    dayName,
                    getMessage("weather.max.temp", tempMax, tempMin),
                    getMessage("weather.precip.probability", precipProb),
                    weatherDesc
            ));
        }

        if (response.current() != null) {
            var current = response.current();
            sb.append(getMessage("weather.current.status",
                    current.temperature_2m(),
                    getWeatherDescriptionText(current.weather_code())));
        }

        return sb.toString();
    }
}
