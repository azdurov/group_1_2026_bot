package group2026.bot;

import group2026.config.TelegramBotProperties;
import group2026.gismeteo.GismeteoForecastDay;
import group2026.gismeteo.GismeteoWeatherService;
import group2026.holiday.HolidayService;
import group2026.joke.JokeService;
import group2026.meme.MemeResponse;
import group2026.meme.MemeService;
import group2026.openweather.OpenWeatherService;
import group2026.recipe.RecipeResponse;
import group2026.recipe.RecipeService;
import group2026.service.TelegramService;
import group2026.statham.StathamService;
import group2026.wish.WishService;
import group2026.yandex.YandexForecastDay;
import group2026.yandex.YandexWeatherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandHandler implements SpringLongPollingBot {

    private final TelegramBotProperties telegramBotProperties;
    private final YandexWeatherService yandexWeatherService;
    private final GismeteoWeatherService gismeteoWeatherService;
    private final OpenWeatherService openWeatherService;
    private final MemeService memeService;
    private final WishService wishService;
    private final HolidayService holidayService;
    private final StathamService stathamService;
    private final JokeService jokeService;
    private final RecipeService recipeService;
    private final TelegramService telegramService;

    private final Map<String, Consumer<Long>> commands = new HashMap<>();

    @PostConstruct
    void initCommands() {
        registerCommand("/weather", this::handleWeatherCommand);
        registerCommand("/meme", this::handleMemeCommand);
        registerCommand("/wish", this::handleWishCommand);
        registerCommand("/holiday", this::handleHolidayCommand);
        registerCommand("/quote", this::handleQuoteCommand);
        registerCommand("/joke", this::handleJokeCommand);
        registerCommand("/recipe", this::handleRecipeCommand);
    }

    private void registerCommand(String baseCommand, Consumer<Long> handler) {
        commands.put(baseCommand, handler);

        if (telegramBotProperties.username() != null) {
            commands.put(baseCommand + telegramBotProperties.username(), handler);
        }
    }

    @Override
    public String getBotToken() {
        return telegramBotProperties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this::consume;
    }

    private void consume(List<Update> updates) {
        updates.forEach(this::processUpdate);
    }

    private void processUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage();
        long chatId = message.getChatId();
        String text = message.getText();

        log.debug("Received '{}' from {} ({})", text, message.getFrom().getUserName(), chatId);

        commands.getOrDefault(text, this::handleUnknownCommand)
                .accept(chatId);
    }

    private void execute(long chatId, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Command execution failed for chatId={}", chatId, e);
            telegramService.sendMessage(chatId, "❌ Ошибка выполнения команды");
        }
    }

    private void handleUnknownCommand(long chatId) {
        telegramService.sendMessage(chatId, "❓ Неизвестная команда");
    }

    private void handleWeatherCommand(long chatId) {
        execute(chatId, () -> {
            sendOpenWeather(chatId);
        });
    }

    private void sendOpenWeather(long chatId) {
        String message = openWeatherService.formatForecast();
        telegramService.sendMessage(chatId, message);
    }

    private void sendYandexWeather(long chatId) {
        List<YandexForecastDay> forecast = yandexWeatherService.getMorningForecast();
        String message = yandexWeatherService.formatForecast(forecast);
        telegramService.sendMessage(chatId, message);
    }

    private void sendGismeteoWeather(long chatId) {
        List<GismeteoForecastDay> forecast = gismeteoWeatherService.getMorningForecast();
        String message = gismeteoWeatherService.formatForecast(forecast);
        telegramService.sendMessage(chatId, message);
    }

    private void handleMemeCommand(long chatId) {
        execute(chatId, () -> {
            MemeResponse meme = memeService.getRandomMeme();
            telegramService.sendPhoto(chatId, meme.url(), meme.title());
        });
    }

    private void handleWishCommand(long chatId) {
        execute(chatId, () -> telegramService.sendMessage(chatId, wishService.getDailyWish()));
    }

    private void handleHolidayCommand(long chatId) {
        execute(chatId, () -> telegramService.sendMessage(chatId, holidayService.getTodayHoliday()));
    }

    private void handleQuoteCommand(long chatId) {
        execute(chatId, () -> telegramService.sendMessageWithHTML(chatId, stathamService.getFormattedQuote()));
    }

    private void handleJokeCommand(long chatId) {
        execute(chatId, () -> telegramService.sendMessage(chatId, jokeService.getRandomJoke()));
    }

    private void handleRecipeCommand(long chatId) {
        execute(chatId, () -> {
            RecipeResponse recipe = recipeService.getDinnerRecipe();

            telegramService.sendMessage(chatId, recipeService.formatRecipeMessage(recipe));
            telegramService.sendPhoto(chatId, recipe.imageUrl(), recipe.title());
        });
    }
}
