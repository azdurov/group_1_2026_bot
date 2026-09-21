package group2026.reminder.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ParsedCommand(
        LocalDate date,
        LocalTime time,
        RecurrenceRule rule,
        String message
) {

    public boolean isRepeating() {
        return rule != null;
    }
}