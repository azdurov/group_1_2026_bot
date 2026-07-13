package group2026.meme.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemeResponse(
        String title,
        String url,
        String postLink
) {
}
