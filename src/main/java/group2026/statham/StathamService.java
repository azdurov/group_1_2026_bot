package group2026.statham;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StathamService {

    private final StathamClient stathamClient;

    public String getFormattedQuote() {
        String randomQuote = getRandomQuote();
        return """
                💪 Цитата Стэтхема
                
                <tg-spoiler>%s</tg-spoiler>
                """.formatted(randomQuote);
    }

    public String getRandomQuote() {
        try {
            StathamQuoteResponse response = stathamClient.getRandomQuote();

            if (response == null || response.quote() == null || response.quote().isBlank()) {
                return "Не удалось получить цитату 😔";
            }

            return response.quote();
        } catch (Exception e) {
            log.error("Ошибка получения цитаты Стэтхема", e);
            return "Не удалось получить цитату 😔";
        }
    }
}