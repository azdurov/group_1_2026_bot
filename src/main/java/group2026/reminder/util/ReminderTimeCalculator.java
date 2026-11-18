package group2026.reminder.util;

import group2026.config.TimeZones;
import group2026.reminder.model.ParsedCommand;
import group2026.reminder.model.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReminderTimeCalculator {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private static final ZoneId ZONE = TimeZones.DEFAULT;
    private static final LocalTime WINDOW_START = LocalTime.of(8, 0);

    private final Clock clock;

    public LocalDateTime now() {
        return LocalDateTime.now(clock.withZone(ZONE))
                .truncatedTo(ChronoUnit.MINUTES);
    }

    public LocalDateTime startAt(ParsedCommand parsed) {
        LocalDate date = Optional.ofNullable(parsed.date())
                .orElseGet(() -> now().toLocalDate());

        LocalDateTime start = LocalDateTime.of(date, parsed.time());

        if (start.toLocalTime().isBefore(WINDOW_START)) {
            return start.with(WINDOW_START);
        }

        return start;
    }

    public LocalDateTime nextOccurrence(LocalDateTime from, RecurrenceRule rule, LocalDateTime firstAt) {
        if (!from.isAfter(firstAt)) {
            return firstAt;
        }

        long steps = elapsedSteps(firstAt, from, rule) + 1;
        return addSteps(firstAt, rule, steps);
    }

    private long elapsedSteps(LocalDateTime start, LocalDateTime from, RecurrenceRule rule) {
        long interval = rule.interval();
        return switch (rule.frequency()) {
            case DAILY -> ChronoUnit.DAYS.between(start, from) / interval;
            case WEEKLY -> ChronoUnit.WEEKS.between(start, from) / interval;
            case MONTHLY -> ChronoUnit.MONTHS.between(start, from) / interval;
            case YEARLY -> ChronoUnit.YEARS.between(start, from) / interval;
        };
    }

    private LocalDateTime addSteps(LocalDateTime start, RecurrenceRule rule, long steps) {
        long amount = Math.multiplyExact(rule.interval(), steps);

        return switch (rule.frequency()) {
            case DAILY -> start.plusDays(amount);
            case WEEKLY -> start.plusWeeks(amount);
            case MONTHLY -> start.plusMonths(amount);
            case YEARLY -> start.plusYears(amount);
        };
    }

    public String formatTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }
}