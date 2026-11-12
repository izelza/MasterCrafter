package com.bursie;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CraftingService {
    private final ItemCatalog items;
    private final RecipeCatalog recipes;
    private final List<String> targets;
    private final StackPlanner stackPlanner;

    public CraftingService(ItemCatalog items, RecipeCatalog recipes) {
        this.items = items;
        this.recipes = recipes;
        targets = recipes.targetIds();
        stackPlanner = new StackPlanner(items, recipes);
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
            if (game.stack() != null) {
                // Repeating Next cannot bypass an unsolved target or start free play after completion.
                if (!game.stack().running() || game.status() != CraftingSession.Status.SOLVED) return game.challenge();
                var next = challengeFor(game.stack().current().target());
                game.start(next);
                return next;
            }
            var previous = game.challenge();
            String target;
            do {
                target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
            } while (targets.size() > 1 && previous != null && previous.target().id().equals(target));
            var challenge = challengeFor(items.find(target));
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
            if (game.status() == CraftingSession.Status.REVEALED) {
                throw new IllegalStateException("The recipe has been revealed. Get a new recipe to continue.");
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
            if (correct && game.stack() != null && game.stack().running()) {
                game.stack().markSolved(challenge.target().id());
            }
            return result;
        }
    }

    public CraftingSession.Solution reveal(CraftingSession game, String challengeId) {
        synchronized (game) {
            if (game.stack() != null) throw new IllegalStateException("Finish the current recipe or stop The Stack to use Skip.");
            var challenge = game.challenge();
            if (challenge == null || !challenge.id().equals(challengeId)
                    || game.status() == CraftingSession.Status.SOLVED) {
                throw new IllegalStateException("Get a new recipe and try again.");
            }
            if (game.status() == CraftingSession.Status.REVEALED) return game.solution();
            var recipe = recipes.recipesFor(challenge.target().id()).getFirst();
            var grid = recipe.exampleGrid().stream().map(id -> id == null ? null : items.find(id)).toList();
            var solution = new CraftingSession.Solution(challenge.id(), grid,
                    "One correct recipe for " + challenge.target().name() + ". Click Next recipe to continue.");
            game.reveal(solution);
            return solution;
        }
    }

    public GameSnapshot gameState(CraftingSession game) {
        synchronized (game) {
            return new GameSnapshot(game.challenge(), game.status(), game.solution(),
                    game.stack() == null ? StackRun.Snapshot.inactive() : game.stack().snapshot());
        }
    }

    public GameSnapshot startStack(CraftingSession game) {
        synchronized (game) {
            // Safe retry if Start was already processed but its HTTP response was lost.
            if (game.stack() != null && game.stack().running()) return gameState(game);
            var stack = stackPlanner.create();
            game.setStack(stack);
            game.start(challengeFor(stack.current().target()));
            return gameState(game);
        }
    }

    public GameSnapshot stopStack(CraftingSession game) {
        synchronized (game) {
            if (game.stack() != null) {
                game.setStack(null);
                newChallenge(game);
            } else if (game.challenge() == null) {
                newChallenge(game);
            }
            return gameState(game);
        }
    }

    // Debug actions use the real attempt flow, including Stack progress and the stopwatch.
    public DebugResult debug(CraftingSession game, String challengeId, DebugAction action) {
        synchronized (game) {
            if (game.challenge() == null || !game.challenge().id().equals(challengeId)) {
                throw new IllegalStateException("The current recipe has changed. Try again.");
            }
            if (action == DebugAction.COMPLETE_STACK) {
                if (game.stack() == null) throw new IllegalStateException("Start The Stack first.");
                while (game.stack().running()) {
                    if (game.status() == CraftingSession.Status.SOLVED) newChallenge(game);
                    var recipe = recipes.recipesFor(game.challenge().target().id()).getFirst();
                    attempt(game, game.challenge().id(), recipe.exampleGrid());
                }
                return new DebugResult(null, gameState(game));
            }
            var recipe = recipes.recipesFor(game.challenge().target().id()).getFirst();
            var result = attempt(game, challengeId, action == DebugAction.SUCCESS
                    ? recipe.exampleGrid() : Collections.nCopies(9, null));
            return new DebugResult(result, gameState(game));
        }
    }

    public enum DebugAction { SUCCESS, FAILURE, COMPLETE_STACK }
    public record DebugResult(CraftingSession.AttemptResult result, GameSnapshot game) {}

    private static CraftingSession.Challenge challengeFor(ItemCatalog.Item target) {
        return new CraftingSession.Challenge(UUID.randomUUID().toString(), target);
    }

    public record GameSnapshot(CraftingSession.Challenge challenge, CraftingSession.Status status,
                               CraftingSession.Solution solution, StackRun.Snapshot stack) {}
    public record ItemPage(List<ItemCatalog.Item> items, int total, int page, int pages) {}
}
