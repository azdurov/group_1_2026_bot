package group2026.quote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteClient quoteClient;

    public String getFunnyQuote() {
        try {
            Optional<QuoteResponse> quoteOpt = quoteClient.getFunnyQuote();
            if (quoteOpt.isPresent()) {
                QuoteResponse quote = quoteOpt.get();
                return String.format(
                        "%s\n\n— %s",
                        quote.quote(),
                        quote.author() != null ? quote.author() : "Неизвестный автор"
                );
            }
        } catch (Exception e) {
            log.error("Error getting quote", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        throw new RuntimeException("Quote is empty");
    }
}
