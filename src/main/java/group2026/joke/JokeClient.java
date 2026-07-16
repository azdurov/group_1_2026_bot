package group2026.joke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class JokeClient {

    private final RestClient restClient;
    private final JokeProperties properties;

    public JokeResponse getRandomJoke() {

        String body = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("rzhunemogu.ru")
                        .path("/RandJSON.aspx")
                        .queryParam("CType", properties.category())
                        .build())
                .retrieve()
                .body(String.class);


        if (body == null || body.isBlank()) {
            throw new IllegalStateException(
                    "Empty response from joke API"
            );
        }

        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            cleanJson(body),
                            JokeResponse.class
                    );

        } catch (Exception e) {
            log.error("Cannot parse joke response: {}", body, e);

            throw new IllegalStateException(
                    "Failed to parse joke response",
                    e
            );
        }
    }

    private String cleanJson(String json) {

        return json
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "");
    }
}