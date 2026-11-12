package com.bursie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Chooses familiar ingredients for examples without changing the accepted ingredients. */
final class RecipeExamplePreferences {
    private final Preferences preferences;

    RecipeExamplePreferences(ObjectMapper mapper, Map<String, Set<String>> tags, ItemCatalog items) throws IOException {
        try (var input = new ClassPathResource("minecraft/recipe-examples.json").getInputStream()) {
            preferences = mapper.readValue(input, Preferences.class);
        }
        if (preferences.tagDefaults() == null || preferences.alternativePriority() == null) {
            throw new IllegalStateException("Facitfilen måste innehålla tagDefaults och alternativePriority.");
        }
        preferences.tagDefaults().forEach((tag, item) -> {
            if (items.find(item) == null || !tags.getOrDefault(tag, Set.of()).contains(item)) {
                throw new IllegalStateException("Ogiltigt facitval för " + tag + ": " + item);
            }
        });
        for (String item : preferences.alternativePriority()) {
            if (items.find(item) == null) throw new IllegalStateException("Okänt item i facitfilen: " + item);
        }
    }

    String choose(JsonNode ingredient, Set<String> accepted) {
        if (ingredient.isTextual()) {
            String preferred = preferences.tagDefaults().get(ingredient.asText());
            if (preferred != null) return preferred;
        }
        return preferences.alternativePriority().stream().filter(accepted::contains).findFirst()
                .orElseGet(() -> accepted.stream().sorted().findFirst().orElseThrow());
    }

    private record Preferences(Map<String, String> tagDefaults, List<String> alternativePriority) {}
}
