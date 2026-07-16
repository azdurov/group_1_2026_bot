package group2026.statham;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class StathamClient {

    private final StathamProperties properties;
    private final RestClient restClient;

    public StathamQuoteResponse getRandomQuote() {
        return restClient.get()
                .uri(properties.url())
                .retrieve()
                .body(StathamQuoteResponse.class);
    }
}