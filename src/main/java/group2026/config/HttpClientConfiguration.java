package group2026.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class HttpClientConfiguration {

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Moscow"));
    }
}