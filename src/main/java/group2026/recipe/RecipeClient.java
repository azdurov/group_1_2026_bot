package group2026.recipe;

import com.fasterxml.jackson.databind.JsonNode;
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
public class RecipeClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${recipe.api.url}")
    private String recipeApiUrl;

    public Optional<RecipeResponse> getRandomRecipe() {
        log.debug("Fetching random recipe from: {}", recipeApiUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(recipeApiUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode meals = root.path("meals");

                if (meals.isArray() && !meals.isEmpty()) {
                    JsonNode meal = meals.get(0);
                    return Optional.of(new RecipeResponse(
                            meal.path("strMeal").asText(),
                            meal.path("strInstructions").asText(),
                            meal.path("strMealThumb").asText(),
                            meal.path("strArea").asText()
                    ));
                }
            } else {
                log.warn("Failed to fetch recipe. Status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error fetching recipe", e);
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }
}
