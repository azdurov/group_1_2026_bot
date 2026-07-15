package group2026.wish;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final WishClient wishClient;

    public String getDailyWish() {
        try {
            String wish = wishClient.getDailyWish()
                    .orElseThrow(() -> new RuntimeException("Wish is empty"));
            return wish;
        } catch (Exception e) {
            log.error("Error getting wish", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
