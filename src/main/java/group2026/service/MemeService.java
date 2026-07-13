package group2026.service;

import group2026.meme.MemeClient;
import group2026.meme.dto.MemeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemeService {

    private static final Logger log = LoggerFactory.getLogger(MemeService.class);

    private final MemeClient memeClient;

    public MemeService(MemeClient memeClient) {
        this.memeClient = memeClient;
    }

    public Optional<MemeResponse> getRandomMeme() {
        try {
            MemeResponse response = memeClient.getRandomMeme();
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Error getting meme", e);
            return Optional.empty();
        }
    }
}
