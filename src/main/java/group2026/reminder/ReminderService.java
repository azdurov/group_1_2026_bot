package group2026.reminder;

import group2026.reminder.model.ParsedCommand;
import group2026.reminder.model.RecurrenceRule;
import group2026.reminder.model.Reminder;
import group2026.reminder.util.ReminderCommandParser;
import group2026.reminder.util.ReminderTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository repository;
    private final ReminderCommandParser parser;
    private final ReminderTimeCalculator schedule;

    public String help() {
        return """
                ⏰ Напоминания
                
                /reminder <время> <текст> — разовое напоминание: время: HH:mm, дата необязательна: dd.MM HH:mm
                /reminder 18:30 Текст
                /reminder 20.09 18:30 Текст
                
                /reminder <время> каждые <интервал> <текст> — повтор с интервалом: 1д, 1н, 1мес, 1г
                /reminder 10:00 каждые 1д Текст
                /reminder 12:00 каждые 1н Текст
                /reminder 15:00 каждые 1мес Текст
                /reminder 20.09 12:00 каждые 1г Текст
                
                /reminder list — список
                /reminder delete <id> — удалить
                """;
    }

    public String list(long chatId) {
        List<Reminder> reminders = repository.findFromRedis().stream()
                .filter(r -> r.chatId() == chatId)
                .toList();

        if (reminders.isEmpty()) {
            return "Нет напоминаний.";
        }

        StringBuilder result = new StringBuilder("📋 Напоминания:\n\n");
        reminders.forEach(reminder -> appendReminder(result, reminder));
        result.append("Удаление: /reminder delete <id>");

        return result.toString();
    }

    public String add(long chatId, long userId, String args) {
        ParsedCommand parsed;
        try {
            parsed = parser.parse(args);
        } catch (IllegalArgumentException e) {
            return "❌ " + e.getMessage() + "\n\n" + help();
        }

        LocalDateTime now = schedule.now();
        LocalDateTime start = schedule.startAt(parsed);
        String rrule = parsed.isRepeating() ? parsed.rule().toRrule() : null;
        LocalDateTime firstAt = parsed.isRepeating() ? start : null;

        if (!parsed.isRepeating() && !start.isAfter(now)) {
            return "⏰ Указанное время уже прошло";
        }

        LocalDateTime next = resolveNextOccurrence(parsed, start, firstAt, now);

        Reminder reminder = Reminder.create(
                chatId,
                userId,
                parsed.message(),
                rrule,
                firstAt,
                next,
                now
        );

        List<Reminder> reminders = new ArrayList<>(repository.findFromCache());
        reminders.add(reminder);
        repository.saveAll(reminders);

        log.info("Added reminder id={} for chatId={}", reminder.id(), chatId);
        return formatCreated(reminder, parsed);
    }

    public String delete(String id) {
        if (id == null || id.isBlank()) {
            return "Использование: /reminder delete <id>";
        }

        if (!repository.deleteById(id)) {
            log.warn("Reminder id={} not found on delete", id);
            return "❌ Напоминание не найдено";
        }

        log.info("Deleted reminder id={}", id);
        return "🗑 Напоминание удалено";
    }

    private void appendReminder(StringBuilder result, Reminder reminder) {
        String type = reminder.isRepeating()
                ? describe(reminder.rrule())
                : "разовое";

        result.append("id ").append(reminder.id()).append("\n")
                .append("   ").append(reminder.message()).append("\n")
                .append("   ").append(type)
                .append(", ").append(schedule.formatTime(reminder.nextReminderAt()))
                .append("\n\n");
    }

    private LocalDateTime resolveNextOccurrence(
            ParsedCommand parsed, LocalDateTime start, LocalDateTime firstAt, LocalDateTime now
    ) {
        if (!parsed.isRepeating()) {
            return start;
        }

        return schedule.nextOccurrence(now, parsed.rule(), firstAt);
    }

    private String formatCreated(Reminder reminder, ParsedCommand parsed) {
        String type = parsed.isRepeating()
                ? "Повтор: " + parsed.rule().toRussian()
                : "Разовое";

        return "✅ Напоминание создано. Id: `" + reminder.id() + "`\n"
                + type + "\n"
                + "Время: " + schedule.formatTime(reminder.nextReminderAt());
    }

    private String describe(String rrule) {
        try {
            return RecurrenceRule.fromRrule(rrule).toRussian();
        } catch (RuntimeException e) {
            log.warn("Cannot parse rrule '{}'", rrule);
            return "повтор";
        }
    }
}