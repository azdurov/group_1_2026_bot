package group2026.scheduler;

import group2026.config.TelegramBotProperties;
import group2026.gismeteo.GismeteoForecastDay;
import group2026.gismeteo.GismeteoWeatherService;
import group2026.holiday.HolidayService;
import group2026.joke.JokeService;
import group2026.meme.MemeResponse;
import group2026.meme.MemeService;
import group2026.quote.QuoteService;
import group2026.recipe.RecipeResponse;
import group2026.recipe.RecipeService;
import group2026.service.TelegramService;
import group2026.wish.WishService;
import group2026.yandex.YandexForecastDay;
import group2026.yandex.YandexWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class BotScheduler {

    private final TelegramService telegramService;

    private final YandexWeatherService yandexWeatherService;
    private final GismeteoWeatherService gismeteoWeatherService;

    private final MemeService memeService;

    private final WishService wishService;
    private final HolidayService holidayService;
    private final QuoteService quoteService;
    private final JokeService jokeService;
    private final RecipeService recipeService;

    private final TelegramBotProperties telegramBotProperties;


    /**
     * Утренний прогноз:
     * сегодня + 2 дня
     */
    @Scheduled(cron = "${scheduler.morning-weather.cron:0 0 7 * * *}")
    public void sendMorningWeather() {

        execute(
                "morning weather",
                () -> {

                    sendYandexWeather(
                            yandexWeatherService.getMorningForecast()
                    );

                    sendGismeteoWeather(
                            gismeteoWeatherService.getMorningForecast()
                    );
                }
        );
    }


    /**
     * Вечерний прогноз:
     * завтра + 3 дня
     */
    @Scheduled(cron = "${scheduler.evening-weather.cron:0 0 18 * * *}")
    public void sendEveningWeather() {

        execute(
                "evening weather",
                () -> {

                    sendYandexWeather(
                            yandexWeatherService.getEveningForecast()
                    );

                    sendGismeteoWeather(
                            gismeteoWeatherService.getEveningForecast()
                    );
                }
        );
    }


    @Scheduled(cron = "${scheduler.wish.cron:0 0 8 * * *}")
    public void sendDailyWish() {

        execute(
                "daily wish",
                () -> sendMessage(
                        wishService.getDailyWish()
                )
        );
    }


    @Scheduled(cron = "${scheduler.holiday.cron:0 0 9 * * *}")
    public void sendDailyHoliday() {

        execute(
                "daily holiday",
                () -> sendMessage(
                        holidayService.getTodayHoliday()
                )
        );
    }


    @Scheduled(cron = "${scheduler.quote.cron:0 0 10 * * *}")
    public void sendDailyQuote() {

        execute(
                "daily quote",
                () -> sendMessage(
                        quoteService.getFunnyQuote()
                )
        );
    }


    @Scheduled(cron = "${scheduler.joke.cron:0 0 12 * * *}")
    public void sendDailyJoke() {

        execute(
                "daily joke",
                () -> sendMessage(
                        jokeService.getJoke()
                )
        );
    }


    @Scheduled(cron = "${scheduler.recipe.cron:0 0 17 * * *}")
    public void sendDinnerRecipe() {

        execute(
                "dinner recipe",
                () -> {

                    RecipeResponse recipe =
                            recipeService.getDinnerRecipe();


                    sendMessage(
                            recipeService.formatRecipeMessage(recipe)
                    );


                    telegramService.sendScheduledPhoto(
                            recipe.imageUrl(),
                            recipe.title(),
                            telegramBotProperties.chatId()
                    );
                }
        );
    }


    private void sendYandexWeather(
            List<YandexForecastDay> forecast
    ) {

        MemeResponse meme =
                memeService.getRandomMeme();


        String caption =
                """
                        🟦 Яндекс.Погода
                        
                        %s
                        
                        😂 %s
                        """
                        .formatted(
                                yandexWeatherService.formatForecast(forecast),
                                meme.title()
                        );


        telegramService.sendScheduledPhotoWithCaption(
                meme.url(),
                caption,
                telegramBotProperties.chatId()
        );
    }


    private void sendGismeteoWeather(
            List<GismeteoForecastDay> forecast
    ) {

        MemeResponse meme =
                memeService.getRandomMeme();


        String caption =
                """
                        🟩 Gismeteo
                        
                        %s
                        
                        😂 %s
                        """
                        .formatted(
                                formatGismeteoForecast(forecast),
                                meme.title()
                        );


        telegramService.sendScheduledPhotoWithCaption(
                meme.url(),
                caption,
                telegramBotProperties.chatId()
        );
    }


    private String formatGismeteoForecast(
            List<GismeteoForecastDay> forecast
    ) {

        StringBuilder message =
                new StringBuilder();


        forecast.forEach(day ->
                message.append("""
                        
                        📅 %s
                        🌡 %s...%s°C | ☁ %s | 💨 %s м/с | 💧 %s%%
                        
                        """
                        .formatted(
                                day.date(),
                                day.minTemperature(),
                                day.maxTemperature(),
                                day.description(),
                                day.windSpeed(),
                                day.humidity()
                        ))
        );


        return message.toString();
    }


    private void sendMessage(String message) {

        telegramService.sendScheduledMessage(
                message,
                telegramBotProperties.chatId()
        );
    }


    private void execute(
            String taskName,
            Runnable action
    ) {

        try {

            log.info(
                    "Executing scheduled task: {}",
                    taskName
            );

            action.run();

            log.info(
                    "Scheduled task completed: {}",
                    taskName
            );

        } catch (Exception e) {

            log.error(
                    "Scheduled task failed: {}",
                    taskName,
                    e
            );
        }
    }
}