package group2026.bot;

import group2026.openweather.OpenWeatherService;
import group2026.recipe.RecipeResponse;
import group2026.recipe.RecipeService;
import group2026.reminder.ReminderScanService;
import group2026.service.TelegramService;
import group2026.statham.StathamService;
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
    private final OpenWeatherService openWeatherService;
    private final StathamService stathamService;
    private final RecipeService recipeService;
    private final ReminderScanService reminderScanService;

    @Scheduled(cron = "${scheduler.morning-weather.cron}")
    public void sendMorningWeather() {
        execute("morning weather", this::sendOpenWeather);
    }

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

    @Scheduled(cron = "${scheduler.quote.cron}")
    public void sendQuote() {
        execute("statham quote", () ->
                telegramService.sendScheduledMessage(stathamService.getRandomQuote()));
    }

//    @Scheduled(cron = "${scheduler.joke.cron}")
//    public void sendJoke() {
//        execute("joke", () ->
//                sendMessage(jokeService.getRandomJoke()));
//    }

    @Scheduled(cron = "${scheduler.recipe.cron}")
    public void sendRecipe() {
        execute("recipe", () -> {
            RecipeResponse recipe = recipeService.getDinnerRecipe();
            telegramService.sendScheduledMessage(recipeService.formatRecipeMessage(recipe));
            telegramService.sendScheduledPhoto(recipe.imageUrl(), recipe.title());
        });
    }

    @Scheduled(cron = "${scheduler.evening-weather.cron}")
    public void sendEveningWeather() {
        execute("evening weather", this::sendOpenWeather);
    }

    @Scheduled(cron = "${scheduler.reminder-scan.cron}")
    public void scanReminders() {
        execute("reminder scan", reminderScanService::scanAndSend);
    }

    private void sendOpenWeather() {
        telegramService.sendScheduledMessage(openWeatherService.getForecast());
    }

    private void execute(String taskName, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Scheduled task failed: {}", taskName, e);
        }
    }
}
