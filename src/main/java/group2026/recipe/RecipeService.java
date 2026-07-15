package group2026.recipe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeClient recipeClient;

    public RecipeResponse getDinnerRecipe() {
        try {
            return recipeClient.getRandomRecipe()
                    .orElseThrow(() -> new RuntimeException("Recipe is empty"));
        } catch (Exception e) {
            log.error("Error getting recipe", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String formatRecipeMessage(RecipeResponse recipe) {
        if (recipe == null) {
            return "Не доступно";
        }

        String instructions = recipe.instructions();
        // Обрезаем инструкцию, если она слишком длинная
        if (instructions.length() > 1000) {
            instructions = instructions.substring(0, 1000) + "...";
        }

        return String.format(
                "%s\n\n" +
                        "🍽 %s\n" +
                        "🌍 %s\n\n" +
                        "📝 %s",
                "Рецепт",
                recipe.title(),
                recipe.area(),
                instructions
        );
    }
}
