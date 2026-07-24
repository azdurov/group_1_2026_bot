package group2026.config;

import group2026.gismeteo.GismeteoWeatherProperties;
import group2026.joke.JokeProperties;
import group2026.meme.MemeProperties;
import group2026.openweather.OpenWeatherProperties;
import group2026.statham.StathamProperties;
import group2026.weather.WeatherProperties;
import group2026.yandex.YandexWeatherProperties;
import group2026.reminder.ReminderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TelegramBotProperties.class,
        WeatherProperties.class,
        GismeteoWeatherProperties.class,
        OpenWeatherProperties.class,
        YandexWeatherProperties.class,
        JokeProperties.class,
        MemeProperties.class,
        StathamProperties.class,
        ReminderProperties.class,
})
public class PropertiesConfiguration {
}