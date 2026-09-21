package group2026.statham;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StathamQuote(
        String text,
        String sourceName
) {
}