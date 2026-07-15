package group2026.joke;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JokeClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${joke.api.url}")
    private String jokeApiUrl;

    public Optional<JokeResponse> getJoke() {
        log.debug("Fetching joke from: {}", jokeApiUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jokeApiUrl))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), JokeResponse.class));
            } else {
                log.warn("Failed to fetch joke. Status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching joke", e);
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }
}
