package group2026.scheduler;

import group2026.bot.WeatherBot;
import group2026.meme.dto.MemeResponse;
import group2026.service.MemeService;
import group2026.service.WeatherService;
import group2026.weather.dto.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BotScheduler {

    private static final Logger log = LoggerFactory.getLogger(BotScheduler.class);

    private final WeatherBot weatherBot;
    private final WeatherService weatherService;
    private final MemeService memeService;

    public BotScheduler(
            WeatherBot weatherBot,
            WeatherService weatherService,
            MemeService memeService
    ) {
        this.weatherBot = weatherBot;
        this.weatherService = weatherService;
        this.memeService = memeService;
    }

    @Scheduled(cron = "${scheduler.morning.cron:0 0 7 * * *}")
    public void sendMorningWeather() {
        log.info("Sending scheduled morning weather update");
        sendWeatherWithMeme(true);
    }

    @Scheduled(cron = "${scheduler.evening.cron:0 0 18 * * *}")
    public void sendEveningWeather() {
        log.info("Sending scheduled evening weather update");
        sendWeatherWithMeme(false);
    }

    private void sendWeatherWithMeme(boolean isMorning) {
        Optional<WeatherResponse> weatherOpt = weatherService.getWeather();
        Optional<MemeResponse> memeOpt = memeService.getRandomMeme();

        if (weatherOpt.isPresent()) {
            WeatherResponse response = weatherOpt.get();
            String weatherMessage = isMorning
                    ? weatherService.formatMorningForecast(response)
                    : weatherService.formatEveningForecast(response);
            weatherBot.sendScheduledMessage(weatherMessage);
        }

        if (memeOpt.isPresent()) {
            MemeResponse meme = memeOpt.get();
            weatherBot.sendScheduledPhoto(meme.url(), meme.title());
        }
    }
}
