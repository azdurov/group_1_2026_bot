package group2026.wish;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishClient {

    public Optional<String> getDailyWish() {
        return Optional.empty();
    }
}
