package group2026.meme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemeResponse(
        String title,
        String url,
        String postLink,
        String subreddit,
        String author,
        boolean nsfw,
        boolean spoiler
) {
}