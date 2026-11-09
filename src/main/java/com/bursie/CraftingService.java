package com.bursie;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CraftingService {
    private final ItemCatalog items;
    private final RecipeCatalog recipes;
    private final List<String> targets;

    public CraftingService(ItemCatalog items, RecipeCatalog recipes) {
        this.items = items;
        this.recipes = recipes;
        targets = recipes.targetIds();
    }

    public ItemPage searchItems(String query, int page) {
        if (page < 0) throw new IllegalArgumentException("Page must be zero or greater.");
        var matches = items.search(query);
        int pageSize = 48;
        int pages = Math.max(1, (matches.size() + pageSize - 1) / pageSize);
        int current = Math.min(page, pages - 1);
        int start = current * pageSize;
        return new ItemPage(matches.subList(start, Math.min(start + pageSize, matches.size())),
                matches.size(), current, pages);
    }

    public CraftingSession.Challenge newChallenge(CraftingSession game) {
        synchronized (game) {
            var previous = game.challenge();
            String target;
            do {
                target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
            } while (targets.size() > 1 && previous != null && previous.target().id().equals(target));
            var challenge = new CraftingSession.Challenge(UUID.randomUUID().toString(), items.find(target));
            game.start(challenge);
            return challenge;
        }
    }

    public CraftingSession.AttemptResult attempt(CraftingSession game, String challengeId, List<String> grid) {
        if (grid == null || grid.size() != 9
                || grid.stream().anyMatch(id -> id != null && items.find(id) == null)) {
            throw new IllegalArgumentException("Send nine slots containing valid item IDs or null.");
        }
        synchronized (game) {
            var challenge = game.challenge();
            if (challenge == null || !challenge.id().equals(challengeId)) {
                throw new IllegalStateException("Get a new recipe and try again.");
            }
            var previousResult = game.resultFor(grid);
            if (previousResult != null) return previousResult;
            if (game.status() == CraftingSession.Status.SOLVED) {
                throw new IllegalStateException("Get a new recipe and try again.");
            }
            boolean correct = recipes.matches(challenge.target().id(), grid);
            var result = new CraftingSession.AttemptResult(correct, correct
                    ? "Correct! You crafted " + challenge.target().name() + "."
                    : "Not quite! Change the ingredients or their positions and try again.");
            game.recordAttempt(grid, result);
            return result;
        }
    }

    public record ItemPage(List<ItemCatalog.Item> items, int total, int page, int pages) {}
}
