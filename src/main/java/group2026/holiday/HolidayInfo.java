package group2026.holiday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HolidayInfo(
        String date,
        String localName,
        String name,
        String countryCode,
        Boolean fixed,
        String counties,
        String launchYear,
        String[] types,
        Boolean global
) {
}
