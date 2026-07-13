package group2026.meme;

import group2026.meme.dto.MemeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class MemeClient {

    private static final Logger log = LoggerFactory.getLogger(MemeClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String memeApiUrl;

    public MemeClient(
            @Value("${meme.api.url}") String memeApiUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.memeApiUrl = memeApiUrl;
    }

    public MemeResponse getRandomMeme() {
        log.debug("Fetching meme from: {}", memeApiUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(memeApiUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), MemeResponse.class);
            } else {
                log.error("Failed to fetch meme. Status code: {}", response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching meme", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
