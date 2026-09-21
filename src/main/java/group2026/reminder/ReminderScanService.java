package group2026.reminder;

import group2026.reminder.model.RecurrenceRule;
import group2026.reminder.model.Reminder;
import group2026.reminder.util.ReminderTimeCalculator;
import group2026.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderScanService {

    private final ReminderRepository repository;
    private final ReminderTimeCalculator schedule;
    private final TelegramService telegramService;

    public void scanAndSend() {
        List<Reminder> reminders = repository.findFromCache();
        if (reminders.isEmpty()) {
            return;
        }

        LocalDateTime now = schedule.now();
        List<Reminder> remaining = new ArrayList<>(reminders.size());

        for (Reminder reminder : reminders) {
            if (!reminder.isDue(now)) {
                remaining.add(reminder);
                continue;
            }
            log.info("Executing reminder id={} for chatId={}", reminder.id(), reminder.chatId());

            if (reminder.isRepeating()) {
                LocalDateTime nextReminderAt = schedule.nextOccurrence(
                        now, RecurrenceRule.fromRrule(reminder.rrule()), reminder.firstAt()
                );
                remaining.add(reminder.fired(now, nextReminderAt));
            }

            try {
                String text = "⏰ " + reminder.message();
                telegramService.sendMessage(reminder.chatId(), text);
            } catch (RuntimeException e) {
                log.error("Failed to send reminder {}", reminder.id(), e);
                return;
            }

            if (!reminder.isRepeating()) {
                log.info("One-off reminder id={} removed after execution", reminder.id());
            }
        }

        repository.saveAll(remaining);
    }
}