package group2026.bot;

import group2026.config.BotProperties;
import group2026.dto.WeatherMessage;
import group2026.meme.dto.MemeResponse;
import group2026.service.MemeService;
import group2026.service.WeatherService;
import group2026.weather.dto.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

@Component
public class WeatherBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(WeatherBot.class);
    private static final Locale RUSSIAN_LOCALE = new Locale("ru");

    private final BotProperties botProperties;
    private final WeatherService weatherService;
    private final MemeService memeService;
    private final MessageSource messageSource;

    public WeatherBot(
            BotProperties botProperties,
            WeatherService weatherService,
            MemeService memeService,
            org.telegram.telegrambots.meta.TelegramBotsApi telegramBotsApi,
            MessageSource messageSource
    ) {
        this.botProperties = botProperties;
        this.weatherService = weatherService;
        this.memeService = memeService;
        this.messageSource = messageSource;

        try {
            telegramBotsApi.registerBot(this);
            log.info("Bot registered successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to register bot", e);
        }
    }

    @Override
    public String getBotUsername() {
        return messageSource.getMessage("bot.name", null, RUSSIAN_LOCALE);
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, RUSSIAN_LOCALE);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.debug("Received message: {} from chat: {}", messageText, chatId);

            switch (messageText) {
                case "/start" -> handleStartCommand(chatId);
                case "/weather" -> handleWeatherCommand(chatId);
                case "/meme" -> handleMemeCommand(chatId);
                default ->
                        sendMessage(chatId, getMessage("message.error.unknown.command", messageText) + getMessage("message.error.unknown.command.hint"));
            }
        }
    }

    private void handleStartCommand(long chatId) {
        String response = getMessage("message.welcome") + "\n\n" +
                getMessage("message.commands.available") + "\n" +
                getMessage("message.commands.weather") + "\n" +
                getMessage("message.commands.meme");
        sendMessage(chatId, response);
    }

    private void handleWeatherCommand(long chatId) {
        Optional<WeatherResponse> weatherOpt = weatherService.getWeather();

        if (weatherOpt.isPresent()) {
            String message = weatherService.formatWeatherMessage(weatherOpt.get());
            sendMessage(chatId, message);
        } else {
            sendMessage(chatId, getMessage("message.error.weather.failed"));
        }
    }

    private void handleMemeCommand(long chatId) {
        Optional<MemeResponse> memeOpt = memeService.getRandomMeme();

        if (memeOpt.isPresent()) {
            MemeResponse meme = memeOpt.get();
            sendPhotoWithCaption(chatId, meme.url(), meme.title());
        } else {
            sendMessage(chatId, getMessage("message.error.meme.failed"));
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
            log.debug("Message sent to chat {}: {}", chatId, text);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    public void sendScheduledMessage(String text) {
        String chatId = botProperties.getChatId();
        if (chatId == null || chatId.isBlank()) {
            log.warn("Chat ID is not configured, skipping scheduled message");
            return;
        }

        try {
            long parsedChatId = Long.parseLong(chatId);
            sendMessage(parsedChatId, text);
        } catch (NumberFormatException e) {
            log.error("Invalid chat ID format: {}", chatId, e);
        }
    }

    public void sendScheduledPhoto(String imageUrl, String caption) {
        String chatId = botProperties.getChatId();
        if (chatId == null || chatId.isBlank()) {
            log.warn("Chat ID is not configured, skipping scheduled photo");
            return;
        }

        try {
            long parsedChatId = Long.parseLong(chatId);
            sendPhotoWithCaption(parsedChatId, imageUrl, caption);
        } catch (NumberFormatException e) {
            log.error("Invalid chat ID format: {}", chatId, e);
        }
    }

    private void sendPhotoWithCaption(long chatId, String imageUrl, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(imageUrl));
        sendPhoto.setCaption(caption);

        try {
            execute(sendPhoto);
            log.debug("Photo sent to chat {}: {}", chatId, imageUrl);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo", e);
        }
    }
}
