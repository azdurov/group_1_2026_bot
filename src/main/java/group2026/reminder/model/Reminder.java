package group2026.reminder.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Reminder(
        String id,
        long chatId,
        long userId,
        String message,
        String rrule,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime firstAt,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime nextReminderAt,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime lastReminderAt,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime createdAt,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime updatedAt
) {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    public static Reminder create(long chatId, long userId, String message, String rrule,
                                  LocalDateTime firstAt, LocalDateTime nextReminderAt, LocalDateTime now) {
        return new Reminder(
                UUID.randomUUID().toString(),
                chatId,
                userId,
                message,
                rrule,
                firstAt,
                nextReminderAt,
                null,
                now,
                now
        );
    }

    @JsonIgnore
    public boolean isRepeating() {
        return rrule != null && !rrule.isBlank();
    }

    @JsonIgnore
    public boolean isDue(LocalDateTime now) {
        return !nextReminderAt.isAfter(now);
    }

    public Reminder fired(LocalDateTime firedAt, LocalDateTime nextReminderAt) {
        return new Reminder(id, chatId, userId, message, rrule, firstAt, nextReminderAt, firedAt, createdAt, firedAt);
    }
}