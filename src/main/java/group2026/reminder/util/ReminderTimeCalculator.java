package group2026.reminder.util;

import group2026.reminder.model.ParsedCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReminderTimeCalculator {

    private static final long MINUTES_PER_HOUR = 60;
    private static final long MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private static final DateTimeFormatter TIME_OF_DAY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private ZoneId zone;
    private LocalTime windowStart;

    @PostConstruct
    void init() {
        zone = ZoneId.of("Europe/Minsk");
        windowStart = LocalTime.parse("08:00");
    }

    public ZonedDateTime startAt(ParsedCommand parsed) {
        LocalDate date = Optional.ofNullable(parsed.date()).orElseGet(() -> LocalDate.now(zone));

        return ZonedDateTime.of(date, parsed.time(), zone);
    }

    public Instant firstOccurrence(Instant start, long intervalMinutes, Instant now) {
        validateInterval(intervalMinutes);

        if (start.isAfter(now)) {
            return adjustToWindow(start);
        }

        Duration interval = Duration.ofMinutes(intervalMinutes);
        long missedIntervals = Duration.between(start, now).dividedBy(interval) + 1;

        return adjustToWindow(start.plus(interval.multipliedBy(missedIntervals)));
    }

    public Instant nextOccurrence(Instant from, long intervalMinutes) {
        validateInterval(intervalMinutes);

        return adjustToWindow(from.plus(Duration.ofMinutes(intervalMinutes)));
    }

    public String formatTime(Instant instant) {
        return TIME_FORMAT.withZone(zone).format(instant);
    }

    public String formatTimeOfDay(Instant instant) {
        return TIME_OF_DAY_FORMAT.withZone(zone).format(instant);
    }

    public String formatInterval(long minutes) {
        validateInterval(minutes);

        if (minutes % MINUTES_PER_DAY == 0) {
            return minutes / MINUTES_PER_DAY + " дн.";
        }
        if (minutes % MINUTES_PER_HOUR == 0) {
            return minutes / MINUTES_PER_HOUR + " ч";
        }
        return minutes + " мин";
    }

    private void validateInterval(long minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Интервал должен быть положительным");
        }
    }

    private Instant adjustToWindow(Instant instant) {
        ZonedDateTime dateTime = instant.atZone(zone);

        if (dateTime.toLocalTime().isBefore(windowStart)) {
            return ZonedDateTime.of(dateTime.toLocalDate(), windowStart, zone).toInstant();
        }

        return instant;
    }
}