package group2026.statham;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StathamService {

    private final StathamRepository stathamRepository;

    public String getFormattedQuote() {
        try {
            StathamQuote quote = stathamRepository.getRandomQuote();

            if (quote == null || quote.text() == null || quote.text().isBlank()) {
                throw new RuntimeException("Empty quote.");
            }

            return formatQuote(quote);
        } catch (Exception e) {
            log.error("Error getting quote", e);
            return "Не удалось получить цитату 😔";
        }
    }

    private String formatQuote(StathamQuote quote) {
        return """
                💪 Цитата Стэтхема
                
                %s
                """.formatted(quote.text());
    }
}