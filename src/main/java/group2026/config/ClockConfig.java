package group2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    @Bean
    Clock clock() {
        return Clock.system(MOSCOW_ZONE);
    }
}