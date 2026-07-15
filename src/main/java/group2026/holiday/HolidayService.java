package group2026.holiday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayClient holidayClient;

    public String getTodayHoliday() {
        try {
            // Пробуем сначала Беларусь (BY), затем Россию (RU)
            Optional<String> holiday = holidayClient.getTodayHoliday("BY");
            if (holiday.isEmpty()) {
                holiday = holidayClient.getTodayHoliday("RU");
            }

            return holiday.orElse("Пусто");
        } catch (Exception e) {
            log.error("Error getting holiday", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
