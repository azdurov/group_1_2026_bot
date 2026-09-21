package group2026.reminder.util;

import group2026.reminder.model.ParsedCommand;
import group2026.reminder.model.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ReminderCommandParser {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("^(\\d+)\\s*(д|н|мес|г)$");

    private final Clock clock;

    public ParsedCommand parse(String args) {
        if (args == null || args.isBlank()) {
            throw new IllegalArgumentException("Пустая команда");
        }

        String[] tokens = args.trim().split("\\s+");
        int index = 0;
        LocalDate date = null;

        if (tokens[index].contains(".")) {
            date = parseDate(tokens[index++]);
        }

        if (index >= tokens.length) {
            throw new IllegalArgumentException("Не указано время");
        }

        LocalTime time = parseTime(tokens[index++]);
        RecurrenceRule rule = null;

        if (index < tokens.length && "каждые".equalsIgnoreCase(tokens[index])) {
            index++;
            if (index >= tokens.length) {
                throw new IllegalArgumentException("Не указан интервал");
            }
            rule = parseRule(tokens[index++]);
        }

        if (index >= tokens.length) {
            throw new IllegalArgumentException("Не указан текст напоминания");
        }

        String message = String.join(" ", Arrays.copyOfRange(tokens, index, tokens.length));

        return new ParsedCommand(date, time, rule, message);
    }

    private LocalDate parseDate(String token) {
        try {
            return LocalDate.parse(
                    token + "." + LocalDate.now(clock).getYear(),
                    DATE_FORMAT
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректная дата: " + token, e);
        }
    }

    private static LocalTime parseTime(String token) {
        try {
            return LocalTime.parse(token, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректное время: " + token, e);
        }
    }

    private static RecurrenceRule parseRule(String token) {
        Matcher matcher = INTERVAL_PATTERN.matcher(token.toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Некорректный интервал: " + token);
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        RecurrenceRule.Frequency frequency = switch (unit) {
            case "д" -> RecurrenceRule.Frequency.DAILY;
            case "н" -> RecurrenceRule.Frequency.WEEKLY;
            case "мес" -> RecurrenceRule.Frequency.MONTHLY;
            case "г" -> RecurrenceRule.Frequency.YEARLY;
            default -> throw new IllegalArgumentException("Неизвестная единица времени: " + token);
        };

        return RecurrenceRule.of(frequency, value);
    }
}