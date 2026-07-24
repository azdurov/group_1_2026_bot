package group2026.bot;

import group2026.config.TelegramBotProperties;
import group2026.gismeteo.GismeteoWeatherService;
import group2026.holiday.HolidayService;
import group2026.joke.JokeService;
import group2026.meme.MemeService;
import group2026.openweather.OpenWeatherService;
import group2026.recipe.RecipeResponse;
import group2026.recipe.RecipeService;
import group2026.reminder.ReminderService;
import group2026.service.TelegramService;
import group2026.statham.StathamService;
import group2026.wish.WishService;
import group2026.yandex.YandexWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BotScheduler {

    private final TelegramService telegramService;
    private final YandexWeatherService yandexWeatherService;
    private final GismeteoWeatherService gismeteoWeatherService;
    private final OpenWeatherService openWeatherService;
    private final MemeService memeService;
    private final WishService wishService;
    private final HolidayService holidayService;
    private final StathamService stathamService;
    private final JokeService jokeService;
    private final RecipeService recipeService;
    private final ReminderService reminderService;
    private final TelegramBotProperties telegramBotProperties;

    @Scheduled(cron = "${scheduler.morning-weather.cron}")
    public void sendMorningWeather() {
        execute("morning weather", () -> {
            sendOpenWeather();
        });
    }

//    @Scheduled(cron = "${scheduler.wish.cron}")
//    public void sendDailyWish() {
//
//        execute(
//                "daily wish",
//                () -> sendMessage(
//                        wishService.getDailyWish()
//                )
//        );
//    }
//
//
//    @Scheduled(cron = "${scheduler.holiday.cron}")
//    public void sendDailyHoliday() {
//
//        execute(
//                "daily holiday",
//                () -> sendMessage(
//                        holidayService.getTodayHoliday()
//                )
//        );
//    }
//
//    @Scheduled(cron = "${scheduler.quote.cron}")
//    public void sendQuote() {
//        execute("statham quote", () ->
//                telegramService.sendScheduledMessageWithHTML(
//                        stathamService.getFormattedQuote(),
//                        telegramBotProperties.chatId()
//                ));
//    }

    @Scheduled(cron = "${scheduler.joke.cron}")
    public void sendJoke() {
        execute("joke", () ->
                sendMessage(jokeService.getRandomJoke()));
    }

    @Scheduled(cron = "${scheduler.recipe.cron}")
    public void sendRecipe() {
        execute("recipe", () -> {
            RecipeResponse recipe = recipeService.getDinnerRecipe();
            sendMessage(recipeService.formatRecipeMessage(recipe));
            telegramService.sendScheduledPhoto(recipe.imageUrl(), recipe.title(), telegramBotProperties.chatId());
        });
    }

    @Scheduled(cron = "${scheduler.evening-weather.cron}")
    public void sendEveningWeather() {
        execute("evening weather", () -> {
            sendOpenWeather();
        });
    }

    @Scheduled(cron = "${scheduler.reminder.cron}")
    @ConditionalOnProperty(name = "scheduler.reminder.enabled", havingValue = "true", matchIfMissing = true)
    public void sendReminder() {
        execute("reminder", () ->
                sendMessage(reminderService.getReminderMessage())
        );
    }

    private void sendOpenWeather() {
        String message = openWeatherService.getForecast();
        sendMessage(message);
    }

    private void sendMessage(String message) {
        telegramService.sendScheduledMessage(message, telegramBotProperties.chatId());
    }

    private void execute(String taskName, Runnable action) {
        try {
            log.info("Executing scheduled task: {}", taskName);
            action.run();
            log.info("Scheduled task completed: {}", taskName);
        } catch (Exception e) {
            log.error("Scheduled task failed: {}", taskName, e);
        }
    }
}
