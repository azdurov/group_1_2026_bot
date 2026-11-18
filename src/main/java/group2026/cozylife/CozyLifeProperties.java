package group2026.cozylife;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cozylife")
public record CozyLifeProperties(
        String email,
        String password,
        String apiUrl
) {
}