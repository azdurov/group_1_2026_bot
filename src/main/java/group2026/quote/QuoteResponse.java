package group2026.quote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuoteResponse(
        String quote,
        String author,
        String category
) {
}
