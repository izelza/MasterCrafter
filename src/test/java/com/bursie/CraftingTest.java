package com.bursie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CraftingTest {
    @Autowired RecipeCatalog recipes;
    @Autowired ItemCatalog items;
    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mvc;

    @Test
    void everyRealRecipeAcceptsItsIngredientsIncludingTranslationsAndMirrors() {
        int checked = 0;
        for (String target : recipes.targetIds()) {
            for (var recipe : recipes.recipesFor(target)) {
                var original = example(recipe);
                assertThat(recipe.matches(original)).as(recipe.id()).isTrue();
                assertThat(recipes.matches(target, original)).as(recipe.id()).isTrue();
                if (recipe.shaped()) {
                    for (int y = 0; y <= 3 - recipe.height(); y++) {
                        for (int x = 0; x <= 3 - recipe.width(); x++) {
                            var shifted = blank();
                            var mirrored = blank();
                            for (int row = 0; row < recipe.height(); row++) {
                                for (int col = 0; col < recipe.width(); col++) {
                                    String id = original.get(row * 3 + col);
                                    shifted.set((row + y) * 3 + col + x, id);
                                    mirrored.set((row + y) * 3 + recipe.width() - 1 - col + x, id);
                                }
                            }
                            assertThat(recipe.matches(shifted)).as("flyttat %s", recipe.id()).isTrue();
                            assertThat(recipe.matches(mirrored)).as("spegelvänt %s", recipe.id()).isTrue();
                        }
                    }
                } else {
                    Collections.reverse(original);
                    assertThat(recipe.matches(original)).as("omkastat %s", recipe.id()).isTrue();
                }
                checked++;
            }
        }
        assertThat(checked).isGreaterThan(1000);
    }

    @Test
    void shapedRecipeAcceptsTagsButRejectsExtraItemsMissingItemsAndRotations() {
        var grid = Arrays.asList(
                "minecraft:oak_planks", "minecraft:birch_planks", "minecraft:spruce_planks",
                null, "minecraft:stick", null,
                null, "minecraft:stick", null);
        assertThat(recipes.matches("minecraft:wooden_pickaxe", grid)).isTrue();
        var extra = new ArrayList<>(grid);
        extra.set(3, "minecraft:diamond");
        assertThat(recipes.matches("minecraft:wooden_pickaxe", extra)).isFalse();
        var missing = new ArrayList<>(grid);
        missing.set(7, null);
        assertThat(recipes.matches("minecraft:wooden_pickaxe", missing)).isFalse();
        var rotated = blank();
        for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) rotated.set(x * 3 + 2 - y, grid.get(y * 3 + x));
        assertThat(recipes.matches("minecraft:wooden_pickaxe", rotated)).isFalse();
    }

    @Test
    void shapelessMatchingCountsDuplicatesAndHandlesOverlappingAlternatives() {
        var book = Arrays.asList("minecraft:paper", null, "minecraft:leather",
                null, "minecraft:paper", null, null, null, "minecraft:paper");
        assertThat(recipes.matches("minecraft:book", book)).isTrue();
        var missing = new ArrayList<>(book);
        missing.set(8, null);
        assertThat(recipes.matches("minecraft:book", missing)).isFalse();
        var overlap = new CraftingRecipe("overlap", "result", false, 0, 0,
                List.of(Set.of("a", "b"), Set.of("a", "c")));
        var supplied = blank();
        supplied.set(0, "a");
        supplied.set(8, "b");
        assertThat(overlap.matches(supplied)).isTrue();
    }

    @Test
    void allAlternateRecipesForOneTargetAreAccepted() {
        var alternatives = recipes.recipesFor("minecraft:stick");
        assertThat(alternatives.size()).isGreaterThan(1);
        for (var recipe : alternatives) assertThat(recipes.matches("minecraft:stick", example(recipe))).isTrue();
    }

    @Test
    void inventoryCanSearchEnglishNamesAndBrowseEveryItem() throws Exception {
        var found = mapper.readTree(mvc.perform(get("/api/items").param("q", "OAK PLANKS"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        assertThat(found.get("items").findValuesAsText("id")).contains("minecraft:oak_planks");
        assertThat(items.find("minecraft:oak_planks").name()).isEqualTo("Oak Planks");
        JsonNode englishNames;
        try (var input = new org.springframework.core.io.ClassPathResource("minecraft/lang/en_us.json").getInputStream()) {
            englishNames = mapper.readTree(input);
        }
        for (var item : items.items()) {
            assertThat(item.name()).as(item.id()).isEqualTo(englishNames.get(item.id()).asText());
        }
        var seen = new java.util.HashSet<String>();
        int page = 0;
        int pages;
        do {
            var result = mapper.readTree(mvc.perform(get("/api/items").param("page", String.valueOf(page)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
            result.get("items").forEach(item -> assertThat(seen.add(item.get("id").asText())).isTrue());
            pages = result.get("pages").asInt();
            page++;
        } while (page < pages);
        assertThat(seen.size()).isEqualTo(items.items().size());
        mvc.perform(get("/api/items").param("q", "xyz-inte-ett-item"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/items").param("page", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    void challengeCanBeRetriedAndSolvedUsingActualRecipe() throws Exception {
        var session = new MockHttpSession();
        var challenge = newChallenge(session);
        mvc.perform(attempt(challenge, blank()).session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(false));
        var recipe = recipes.recipesFor(challenge.at("/target/id").asText()).getFirst();
        mvc.perform(attempt(challenge, example(recipe)).session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true));
        // Samma HTTP-försök kan upprepas om svaret försvann på nätverket.
        mvc.perform(attempt(challenge, example(recipe)).session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    void rejectsMalformedStaleAndOtherPlayersAttempts() throws Exception {
        var session = new MockHttpSession();
        var challenge = newChallenge(session);
        mvc.perform(attempt(challenge, List.of("minecraft:stick")).session(session)).andExpect(status().isBadRequest());
        var unknown = blank();
        unknown.set(0, "minecraft:made_up_item");
        mvc.perform(attempt(challenge, unknown).session(session)).andExpect(status().isBadRequest());
        mvc.perform(attempt(challenge, blank()).session(new MockHttpSession())).andExpect(status().isConflict());
        var next = newChallenge(session);
        assertThat(next.at("/target/id").asText()).isNotEqualTo(challenge.at("/target/id").asText());
        mvc.perform(attempt(challenge, blank()).session(session)).andExpect(status().isConflict());
        mvc.perform(attempt(next, blank()).session(session)).andExpect(status().isOk());
    }

    private JsonNode newChallenge(MockHttpSession session) throws Exception {
        return mapper.readTree(mvc.perform(post("/api/crafting/challenges").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder attempt(
            JsonNode challenge, List<String> grid) throws Exception {
        return post("/api/crafting/attempts").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("challengeId", challenge.get("id").asText(), "grid", grid)));
    }

    private static ArrayList<String> blank() {
        return new ArrayList<>(Collections.nCopies(9, null));
    }

    private static ArrayList<String> example(CraftingRecipe recipe) {
        var grid = blank();
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            var ingredient = recipe.ingredients().get(i);
            if (!ingredient.isEmpty()) {
                int slot = recipe.shaped() ? (i / recipe.width()) * 3 + i % recipe.width() : i;
                grid.set(slot, ingredient.stream().sorted().findFirst().orElseThrow());
            }
        }
        return grid;
    }
}
