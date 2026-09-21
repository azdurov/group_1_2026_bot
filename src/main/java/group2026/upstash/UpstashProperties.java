package group2026.upstash;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "upstash")
public record UpstashProperties(
        String url,
        String token
) {
}