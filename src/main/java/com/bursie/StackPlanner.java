package com.bursie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Samples targets by their cheapest valid recipe; every target can appear only once. */
final class StackPlanner {
    private final Map<Integer, List<StackRun.Entry>> pools = new HashMap<>();

    StackPlanner(ItemCatalog items, RecipeCatalog recipes) {
        for (String target : recipes.targetIds()) {
            int minimum = recipes.recipesFor(target).stream().mapToInt(CraftingRecipe::difficulty).min().orElseThrow();
            pools.computeIfAbsent(minimum, ignored -> new ArrayList<>()).add(new StackRun.Entry(items.find(target), minimum));
        }
    }

    StackRun create() {
        var deck = new ArrayList<StackRun.Entry>();
        sample(deck, 1, 19);
        sample(deck, 2, 20);
        sample(deck, 3, 20);
        deck.addAll(pools.getOrDefault(4, List.of()));
        deck.addAll(pools.getOrDefault(5, List.of()));
        Collections.shuffle(deck, ThreadLocalRandom.current());
        return new StackRun(deck);
    }

    private void sample(List<StackRun.Entry> deck, int difficulty, int count) {
        var candidates = new ArrayList<>(pools.getOrDefault(difficulty, List.of()));
        if (candidates.size() < count) throw new IllegalStateException("Not enough targets for The Stack level " + difficulty);
        Collections.shuffle(candidates, ThreadLocalRandom.current());
        deck.addAll(candidates.subList(0, count));
    }
}
