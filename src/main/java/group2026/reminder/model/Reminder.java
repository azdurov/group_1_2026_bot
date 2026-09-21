package group2026.reminder.model;

import java.time.Instant;
import java.util.UUID;

public record Reminder(
        String id,
        long chatId,
        long userId,
        String message,
        long intervalMinutes,
        Instant nextReminderAt,
        Instant lastReminderAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static Reminder create(long chatId, long userId, String message, long intervalMinutes,
                                  Instant nextReminderAt, Instant now) {
        return new Reminder(
                UUID.randomUUID().toString(),
                chatId,
                userId,
                message,
                intervalMinutes,
                nextReminderAt,
                null,
                now,
                now
        );
    }

    public boolean isDue(Instant now) {
        return !nextReminderAt.isAfter(now);
    }

    public Reminder fired(Instant firedAt, Instant nextReminderAt) {
        return new Reminder(id, chatId, userId, message, intervalMinutes, nextReminderAt, firedAt, createdAt, firedAt);
    }
}