package group2026.reminder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderProperties reminderProperties;

    public String getReminderMessage() {
        return reminderProperties.message();
    }
}
