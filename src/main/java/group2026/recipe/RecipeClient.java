package group2026.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group2026.http.ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeClient {

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    @Value("${recipe.api.url}")
    private String recipeApiUrl;

    public Optional<RecipeResponse> getRandomRecipe() {
        log.debug("Fetching random recipe from: {}", recipeApiUrl);

        String body = apiClient.get(recipeApiUrl, String.class);

        JsonNode meals;
        try {
            meals = objectMapper.readTree(body).path("meals");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось разобрать ответ", e);
        }

        if (!meals.isArray() || meals.isEmpty()) {
            return Optional.empty();
        }

        JsonNode meal = meals.get(0);
        return Optional.of(new RecipeResponse(
                meal.path("strMeal").asText(),
                meal.path("strInstructions").asText(),
                meal.path("strMealThumb").asText(),
                meal.path("strArea").asText()
        ));
    }
}