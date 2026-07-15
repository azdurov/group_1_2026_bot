package group2026.holiday;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${holiday.api.url}")
    private String holidayApiUrl;

    public Optional<String> getTodayHoliday(String countryCode) {
        String year = String.valueOf(LocalDate.now().getYear());
        String url = holidayApiUrl + "/" + year + "/" + countryCode;

        log.debug("Fetching holidays for {} (country: {})", year, countryCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var holidays = objectMapper.readValue(response.body(), HolidayInfo[].class);
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

                for (HolidayInfo holiday : holidays) {
                    if (holiday.date().equals(today)) {
                        return Optional.of(holiday.localName());
                    }
                }
            } else {
                log.warn("Failed to fetch holidays. Status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching holidays", e);
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }
}
