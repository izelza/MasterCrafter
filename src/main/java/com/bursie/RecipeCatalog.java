package com.bursie;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.TreeMap;

@Component
public class RecipeCatalog {
    private final Map<String, List<CraftingRecipe>> byResult;
    private final Map<Integer, List<CraftingRecipe>> byDifficulty;

    public RecipeCatalog(ObjectMapper mapper, ItemCatalog items) throws IOException {
        Map<String, Set<String>> tags;
        try (var input = new ClassPathResource("minecraft/item-tags.json").getInputStream()) {
            tags = mapper.readValue(input, new TypeReference<>() {});
        }
        var examples = new RecipeExamplePreferences(mapper, tags, items);
        var loaded = new LinkedHashMap<String, List<CraftingRecipe>>();
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:minecraft/recipes/*.json");
        for (var resource : resources) {
            JsonNode json;
            try (var input = resource.getInputStream()) {
                json = mapper.readTree(input);
            }
            boolean shaped = switch (json.path("type").asText()) {
                case "minecraft:crafting_shaped" -> true;
                case "minecraft:crafting_shapeless" -> false;
                default -> throw new IllegalStateException("Okänd recepttyp i " + resource.getFilename());
            };
            String result = json.path("result").path("id").asText();
            // Äldre IDE-/Maven-byggen kan ha kvar kopior av de arkiverade mallrecepten.
            if (result.endsWith("_smithing_template")) continue;
            if (items.find(result) == null) throw new IllegalStateException("Okänt receptresultat: " + result);
            var ingredients = new ArrayList<Set<String>>();
            var exampleIngredients = new ArrayList<String>();
            int width = 0;
            int height = 0;
            if (shaped) {
                var pattern = json.path("pattern");
                height = pattern.size();
                width = pattern.get(0).asText().length();
                for (var row : pattern) {
                    if (row.asText().length() != width) throw new IllegalStateException("Ojämnt receptmönster");
                    for (char symbol : row.asText().toCharArray()) {
                        if (symbol == ' ') {
                            ingredients.add(Set.of());
                            exampleIngredients.add(null);
                        } else {
                            var ingredient = json.path("key").get(String.valueOf(symbol));
                            var accepted = expand(ingredient, tags, items);
                            ingredients.add(accepted);
                            exampleIngredients.add(examples.choose(ingredient, accepted));
                        }
                    }
                }
            } else {
                for (var ingredient : json.path("ingredients")) {
                    var accepted = expand(ingredient, tags, items);
                    ingredients.add(accepted);
                    exampleIngredients.add(examples.choose(ingredient, accepted));
                }
            }
            if (ingredients.isEmpty() || ingredients.size() > 9 || width > 3 || height > 3) {
                throw new IllegalStateException("Receptet ryms inte i crafting-griden: " + resource.getFilename());
            }
            var recipe = new CraftingRecipe(resource.getFilename(), result, shaped, width, height, ingredients, exampleIngredients);
            loaded.computeIfAbsent(result, ignored -> new ArrayList<>()).add(recipe);
        }
        if (loaded.isEmpty()) throw new IllegalStateException("Inga crafting-recept hittades.");
        loaded.replaceAll((id, recipes) -> List.copyOf(recipes));
        byResult = Map.copyOf(loaded);
        var grouped = new TreeMap<Integer, List<CraftingRecipe>>();
        for (var resultRecipes : loaded.values()) {
            for (var recipe : resultRecipes) {
                grouped.computeIfAbsent(recipe.difficulty(), ignored -> new ArrayList<>()).add(recipe);
            }
        }
        grouped.replaceAll((difficulty, recipes) -> List.copyOf(recipes));
        byDifficulty = Collections.unmodifiableMap(grouped);
    }

    private static Set<String> expand(JsonNode node, Map<String, Set<String>> tags, ItemCatalog items) {
        if (node == null) throw new IllegalStateException("Ingrediens saknas i recept");
        var accepted = new HashSet<String>();
        if (node.isArray()) {
            for (var alternative : node) accepted.addAll(expand(alternative, tags, items));
        } else if (node.isTextual()) {
            String id = node.asText();
            if (id.startsWith("#")) accepted.addAll(tags.getOrDefault(id, Set.of()));
            else accepted.add(id);
        }
        if (accepted.isEmpty() || accepted.stream().anyMatch(id -> items.find(id) == null)) {
            throw new IllegalStateException("Okänd eller tom ingrediens: " + node);
        }
        return Set.copyOf(accepted);
    }

    public List<String> targetIds() {
        return byResult.keySet().stream().sorted().toList();
    }

    public List<CraftingRecipe> recipesFor(String resultId) {
        return byResult.getOrDefault(resultId, List.of());
    }

    public Map<Integer, List<CraftingRecipe>> recipesByDifficulty() {
        return byDifficulty;
    }

    public boolean matches(String resultId, List<String> grid) {
        return recipesFor(resultId).stream().anyMatch(recipe -> recipe.matches(grid));
    }
}
