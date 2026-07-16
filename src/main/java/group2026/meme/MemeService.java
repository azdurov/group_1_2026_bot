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

        MemeResponse meme = memeClient.getRandomMeme();

        if (meme.url() == null || meme.url().isBlank()) {
            throw new RuntimeException("Meme image url is empty");
        }

        if (meme.nsfw() || meme.spoiler()) {
            return getRandomMeme();
        }

        return meme;
    }

    public String formatMemeCaption(MemeResponse meme) {

        return """
                %s
                
                источник: %s
                """
                .formatted(
                        meme.title(),
                        meme.subreddit()
                );
    }
}