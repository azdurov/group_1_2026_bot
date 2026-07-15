package group2026.quote;

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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${quote.api.url}")
    private String quoteApiUrl;
    @Value("${quote.api.key:}")
    private String quoteApiKey;

    public Optional<QuoteResponse> getFunnyQuote() {
        log.debug("Fetching funny quote from: {}", quoteApiUrl);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(quoteApiUrl))
                .GET();

        if (quoteApiKey != null && !quoteApiKey.isBlank()) {
            builder.header("X-Api-Key", quoteApiKey);
        }

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var quotes = objectMapper.readValue(response.body(), QuoteResponse[].class);
                if (quotes.length > 0) {
                    return Optional.of(quotes[0]);
                }
            } else {
                log.warn("Failed to fetch quote. Status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching quote", e);
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }
}
