package group2026.meme;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meme")
public record MemeProperties(
        String apiUrl
) {
}