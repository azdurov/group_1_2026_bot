package group2026.meme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemeService {

    private final MemeClient memeClient;

    public MemeResponse getRandomMeme() {
        try {
            MemeResponse response = memeClient.getRandomMeme();
            if (response == null) {
                throw new RuntimeException("Meme response is null");
            }
            return response;
        } catch (Exception e) {
            log.error("Error getting meme", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
