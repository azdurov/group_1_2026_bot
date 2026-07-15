package group2026.config;

import group2026.gismeteo.GismeteoWeatherProperties;
import group2026.weather.WeatherProperties;
import group2026.yandex.YandexWeatherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TelegramBotProperties.class,
        WeatherProperties.class,
        GismeteoWeatherProperties.class,
        YandexWeatherProperties.class,
})
public class PropertiesConfiguration {
}