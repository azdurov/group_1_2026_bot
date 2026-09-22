package group2026.reminder.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

import static group2026.config.ObjectMapperConfig.DATE_TIME_PATTERN;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reminder {

    private final String id;

    private final long chatId;

    private final long userId;

    private final String message;

    private final String rrule;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime firstAt;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime nextReminderAt;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime lastReminderAt;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updatedAt;

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

    public void fired(LocalDateTime firedAt, LocalDateTime nextReminderAt) {
        this.nextReminderAt = nextReminderAt;
        this.lastReminderAt = firedAt;
        this.updatedAt = firedAt;
    }
}