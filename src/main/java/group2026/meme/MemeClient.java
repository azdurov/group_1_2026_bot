package group2026.meme;

import group2026.http.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemeClient {

    private final ApiClient apiClient;
    private final MemeProperties properties;

    public MemeResponse getRandomMeme() {
        return apiClient.get(properties.apiUrl(), MemeResponse.class);
    }
}