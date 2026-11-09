package com.bursie;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class CraftingController {
    private static final String GAME_SESSION = "crafting.game";
    private final CraftingService crafting;

    public CraftingController(CraftingService crafting) {
        this.crafting = crafting;
    }

    @GetMapping("/api/items")
    public CraftingService.ItemPage search(@RequestParam(defaultValue = "") String q,
                                          @RequestParam(defaultValue = "0") int page) {
        try {
            return crafting.searchItems(q, page);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/api/crafting/challenges")
    public CraftingSession.Challenge newChallenge(HttpSession session) {
        return crafting.newChallenge(gameSession(session));
    }

    @PostMapping("/api/crafting/attempts")
    public CraftingSession.AttemptResult attempt(@RequestBody Attempt attempt, HttpSession session) {
        try {
            return crafting.attempt(gameSession(session), attempt.challengeId(), attempt.grid());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    @PostMapping("/api/crafting/challenges/{challengeId}/solution")
    public CraftingSession.Solution reveal(@PathVariable String challengeId, HttpSession session) {
        try {
            return crafting.reveal(gameSession(session), challengeId);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    // HTTP-sessionen hör hemma i webblagret. Servicen får bara spelmodellen.
    private CraftingSession gameSession(HttpSession session) {
        synchronized (session) {
            var game = (CraftingSession) session.getAttribute(GAME_SESSION);
            if (game == null) {
                game = new CraftingSession();
                session.setAttribute(GAME_SESSION, game);
            }
            return game;
        }
    }

    public record Attempt(String challengeId, List<String> grid) {}
}
