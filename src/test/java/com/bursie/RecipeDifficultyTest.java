package com.bursie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeDifficultyTest {
    private static RecipeCatalog recipes;

    @BeforeAll
    static void loadData() throws IOException {
        var mapper = new ObjectMapper();
        recipes = new RecipeCatalog(mapper, new ItemCatalog(mapper));
    }

    @Test
    void familiarRecipesCountIngredientTypesRatherThanAmountsOrEmptySlots() {
        var expected = Map.of("crafting_table", 1, "slime_block", 1, "book", 2,
                "wooden_pickaxe", 2, "campfire", 3, "piston", 4, "cake", 4);
        expected.forEach((result, difficulty) -> assertThat(recipe(result).difficulty()).as(result).isEqualTo(difficulty));
    }

    @Test
    void tagsAndExplicitAlternativeListsAreSingleIngredientTypes() {
        assertThat(recipe("campfire").difficulty()).isEqualTo(3); // stick + coals + logs
        assertThat(recipe("tnt").difficulty()).isEqualTo(2); // gunpowder + sand/red sand
        assertThat(recipe("torch").difficulty()).isEqualTo(2); // stick + coal/charcoal
        assertThat(recipe("quartz_stairs").difficulty()).isEqualTo(1); // alternative quartz blocks
        assertThat(recipe("painting").difficulty()).isEqualTo(2); // sticks + any wool color
    }

    @Test
    void everyActiveRecipeAppearsInExactlyOneDifficultyCategory() {
        var all = recipes.targetIds().stream().flatMap(target -> recipes.recipesFor(target).stream()).toList();
        var grouped = recipes.recipesByDifficulty();
        var classified = grouped.values().stream().flatMap(List::stream).toList();
        assertThat(classified).containsExactlyInAnyOrderElementsOf(all).doesNotHaveDuplicates();
        assertThat(classified.size()).isGreaterThan(1000);
        grouped.forEach((difficulty, category) -> {
            assertThat(difficulty).isBetween(1, 9);
            assertThat(category).isNotEmpty().allSatisfy(recipe -> assertThat(recipe.difficulty()).isEqualTo(difficulty));
        });
    }

    private static CraftingRecipe recipe(String result) {
        return recipes.recipesFor("minecraft:" + result).stream()
                .filter(recipe -> recipe.id().equals(result + ".json"))
                .findFirst().orElseThrow();
    }
}
