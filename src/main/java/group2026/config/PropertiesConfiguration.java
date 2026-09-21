package group2026.config;

import group2026.joke.JokeProperties;
import group2026.meme.MemeProperties;
import group2026.openweather.OpenWeatherProperties;
import group2026.statham.StathamProperties;
import group2026.upstash.UpstashProperties;
import group2026.weather.WeatherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TelegramBotProperties.class,
        WeatherProperties.class,
        OpenWeatherProperties.class,
        JokeProperties.class,
        MemeProperties.class,
        StathamProperties.class,
        UpstashProperties.class,
})
public class PropertiesConfiguration {
}