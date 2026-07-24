package group2026.reminder;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reminder")
public record ReminderProperties(
        String message
) {
}
