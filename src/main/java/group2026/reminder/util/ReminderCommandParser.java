package group2026.reminder.util;

import group2026.reminder.model.ParsedCommand;
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

@Component
@RequiredArgsConstructor
public class ReminderCommandParser {

    private static final long MINUTES_PER_HOUR = 60;
    private static final long MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String EVERY = "каждые";

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
        long intervalMinutes = 0;

        if (index < tokens.length && EVERY.equalsIgnoreCase(tokens[index])) {
            index++;
            if (index >= tokens.length) {
                throw new IllegalArgumentException("Не указан интервал");
            }
            intervalMinutes = parseInterval(tokens[index++]);
        }

        if (index >= tokens.length) {
            throw new IllegalArgumentException("Не указан текст напоминания");
        }

        String message = String.join(" ", Arrays.copyOfRange(tokens, index, tokens.length));

        return new ParsedCommand(date, time, intervalMinutes, message);
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

    private LocalTime parseTime(String token) {
        try {
            return LocalTime.parse(token, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректное время: " + token, e);
        }
    }

    private static long parseInterval(String token) {
        if (token.isBlank()) {
            throw new IllegalArgumentException("Пустой интервал");
        }

        String value = token.toLowerCase(Locale.ROOT);
        long multiplier = 1;

        if (value.endsWith("ч") || value.endsWith("h") || value.endsWith("д") || value.endsWith("d")) {
            multiplier = switch (value.charAt(value.length() - 1)) {
                case 'ч', 'h' -> MINUTES_PER_HOUR;
                case 'д', 'd' -> MINUTES_PER_DAY;
                default -> throw new IllegalArgumentException("Неизвестная единица времени");
            };
            value = value.substring(0, value.length() - 1);
        }

        try {
            long amount = Long.parseLong(value);
            return Math.multiplyExact(amount, multiplier);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Некорректный интервал: " + token, e);
        }
    }
}