package group2026.joke;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "joke")
public record JokeProperties(
        String apiUrl,
        Integer category
) {
}