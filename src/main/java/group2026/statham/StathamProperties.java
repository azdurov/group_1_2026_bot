package group2026.statham;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statham")
public record StathamProperties(
        String url
) {
}