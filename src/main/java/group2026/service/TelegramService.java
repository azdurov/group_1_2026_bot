package group2026.service;

import group2026.config.TelegramBotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private static final int MAX_CAPTION_LENGTH = 1024;
    private static final int MAX_MESSAGE_LENGTH = 4096;

    private final TelegramClient telegramClient;
    private final TelegramBotProperties telegramBotProperties;

    public void sendMessage(long chatId, String text) {
        sendMessageWithParsing(chatId, text, null);
    }

    public void sendMessageWithHTML(long chatId, String text) {
        sendMessageWithParsing(chatId, text, ParseMode.HTML);
    }

    public void sendMessageWithParsing(long chatId, String text, String parseMode) {
        if (text == null || text.isBlank()) {
            log.warn("Skip empty message for chat {}", chatId);
            return;
        }

        try {
            String truncated = truncate(text, MAX_MESSAGE_LENGTH);
            SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                    .chatId(chatId)
                    .text(truncated);

            if (parseMode != null) {
                builder.parseMode(parseMode);
            }
            SendMessage message = builder.build();

            telegramClient.execute(message);

            log.debug("Message sent to chat {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }

    public void sendPhoto(long chatId, String imageUrl, String caption) {
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("Skip empty photo for chat {}", chatId);
            return;
        }

        SendPhoto photo = new SendPhoto(String.valueOf(chatId), new InputFile(imageUrl));

        if (caption != null && !caption.isBlank()) {
            photo.setCaption(truncate(caption, MAX_CAPTION_LENGTH));
        }

        try {
            telegramClient.execute(photo);
            log.debug("Photo sent to chat {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo to chat {}", chatId, e);
        }
    }

    public void sendScheduledMessage(String text) {
        sendMessage(resolveChatId(), text);
    }

    public void sendScheduledMessageWithHTML(String text) {
        sendMessageWithHTML(resolveChatId(), text);
    }

    public void sendScheduledPhoto(String imageUrl, String caption) {
        sendPhoto(resolveChatId(), imageUrl, caption);
    }

    private long resolveChatId() {
        String chatId = telegramBotProperties.chatId();
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalStateException("Chat ID is not configured");
        }

        try {
            return Long.parseLong(chatId);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid chat ID format: " + chatId, e);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
