package com.bursie;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/** Ett recept: varje ingrediens innehåller de item-ID:n som accepteras i den rutan. */
public record CraftingRecipe(String id, String resultId, boolean shaped, int width, int height,
                             List<Set<String>> ingredients, List<String> exampleIngredients) {
    public CraftingRecipe {
        ingredients = List.copyOf(ingredients);
        exampleIngredients = Collections.unmodifiableList(new ArrayList<>(exampleIngredients));
        if (ingredients.size() != exampleIngredients.size()) {
            throw new IllegalArgumentException("Facit och recept måste ha lika många rutor: " + id);
        }
        for (int index = 0; index < ingredients.size(); index++) {
            var accepted = ingredients.get(index);
            var example = exampleIngredients.get(index);
            if (accepted.isEmpty() ? example != null : example == null || !accepted.contains(example)) {
                throw new IllegalArgumentException("Facit innehåller en ogiltig ingrediens: " + id);
            }
        }
    }

    /** Ett konkret facit; taggar och alternativa ingredienser får ett giltigt item var. */
    public List<String> exampleGrid() {
        var grid = new ArrayList<String>(Collections.nCopies(9, null));
        for (int index = 0; index < ingredients.size(); index++) {
            String example = exampleIngredients.get(index);
            if (example != null) {
                int slot = shaped ? (index / width) * 3 + index % width : index;
                grid.set(slot, example);
            }
        }
        return Collections.unmodifiableList(grid);
    }

    public boolean matches(List<String> grid) {
        if (grid == null || grid.size() != 9) return false;
        if (!shaped) {
            var supplied = grid.stream().filter(item -> item != null).toList();
            if (supplied.size() != ingredients.size()) return false;
            // Smala alternativ först, men prova även andra fördelningar när taggar överlappar.
            var ordered = ingredients.stream().sorted(Comparator.comparingInt(Set::size)).toList();
            return assign(ordered, supplied, new boolean[supplied.size()], 0);
        }
        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesAt(grid, x, y, false) || matchesAt(grid, x, y, true)) return true;
            }
        }
        return false;
    }

    private boolean matchesAt(List<String> grid, int offsetX, int offsetY, boolean mirror) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int localX = x - offsetX;
                int localY = y - offsetY;
                Set<String> accepted = Set.of();
                if (localX >= 0 && localX < width && localY >= 0 && localY < height) {
                    int recipeX = mirror ? width - 1 - localX : localX;
                    accepted = ingredients.get(localY * width + recipeX);
                }
                String supplied = grid.get(y * 3 + x);
                if (accepted.isEmpty() ? supplied != null : supplied == null || !accepted.contains(supplied)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean assign(List<Set<String>> ingredients, List<String> supplied, boolean[] used, int index) {
        if (index == ingredients.size()) return true;
        for (int slot = 0; slot < supplied.size(); slot++) {
            if (!used[slot] && ingredients.get(index).contains(supplied.get(slot))) {
                used[slot] = true;
                if (assign(ingredients, supplied, used, index + 1)) return true;
                used[slot] = false;
            }
        }
        return false;
    }
}
