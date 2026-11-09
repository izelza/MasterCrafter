package com.bursie;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class CraftingController {
    private static final String ACTIVE_CHALLENGE = "crafting.activeChallenge";
    private final ItemCatalog items;
    private final RecipeCatalog recipes;
    private final List<String> targets;

    public CraftingController(ItemCatalog items, RecipeCatalog recipes) {
        this.items = items;
        this.recipes = recipes;
        targets = recipes.targetIds();
    }

    @GetMapping("/api/items")
    public ItemPage search(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page) {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be zero or greater.");
        var matches = items.search(q);
        int pageSize = 48;
        int pages = Math.max(1, (matches.size() + pageSize - 1) / pageSize);
        int current = Math.min(page, pages - 1);
        int start = current * pageSize;
        return new ItemPage(matches.subList(start, Math.min(start + pageSize, matches.size())), matches.size(), current, pages);
    }

    @PostMapping("/api/crafting/challenges")
    public Challenge newChallenge(HttpSession session) {
        synchronized (session) {
            var previous = (Challenge) session.getAttribute(ACTIVE_CHALLENGE);
            String target;
            do {
                target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
            } while (targets.size() > 1 && previous != null && previous.target().id().equals(target));
            var challenge = new Challenge(UUID.randomUUID().toString(), items.find(target));
            session.setAttribute(ACTIVE_CHALLENGE, challenge);
            return challenge;
        }
    }

    @PostMapping("/api/crafting/attempts")
    public AttemptResult attempt(@RequestBody Attempt attempt, HttpSession session) {
        if (attempt.grid() == null || attempt.grid().size() != 9
                || attempt.grid().stream().anyMatch(id -> id != null && items.find(id) == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Send nine slots containing valid item IDs or null.");
        }
        synchronized (session) {
            var challenge = (Challenge) session.getAttribute(ACTIVE_CHALLENGE);
            if (challenge == null || !challenge.id().equals(attempt.challengeId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Get a new recipe and try again.");
            }
            boolean correct = recipes.matches(challenge.target().id(), attempt.grid());
            // Behåll frågan så att fel går att rätta och nätverksförsök kan skickas igen.
            return new AttemptResult(correct, correct
                    ? "Correct! You crafted " + challenge.target().name() + "."
                    : "Not quite! Change the ingredients or their positions and try again.");
        }
    }

    public record ItemPage(List<ItemCatalog.Item> items, int total, int page, int pages) {}
    public record Challenge(String id, ItemCatalog.Item target) {}
    public record Attempt(String challengeId, List<String> grid) {}
    public record AttemptResult(boolean correct, String message) {}
}
