package group2026.holiday;

import group2026.http.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayClient {

    private final ApiClient apiClient;
    @Value("${holiday.api.url}")
    private String holidayApiUrl;

    public Optional<String> getTodayHoliday(String countryCode) {
        String year = String.valueOf(LocalDate.now().getYear());
        String url = holidayApiUrl + "/" + year + "/" + countryCode;
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        log.debug("Fetching holidays for {} (country: {})", year, countryCode);

        HolidayInfo[] holidays = apiClient.get(url, HolidayInfo[].class);

        for (HolidayInfo holiday : holidays) {
            if (today.equals(holiday.date())) {
                return Optional.of(holiday.localName());
            }
        }

        return Optional.empty();
    }
}