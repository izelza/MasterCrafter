package com.bursie;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/api/crafting/game")
    public ResponseEntity<CraftingService.GameSnapshot> state(HttpSession session) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(crafting.gameState(gameSession(session)));
    }

    @PostMapping("/api/crafting/stack/start")
    public CraftingService.GameSnapshot startStack(HttpSession session) {
        return crafting.startStack(gameSession(session));
    }

    @PostMapping("/api/crafting/stack/stop")
    public CraftingService.GameSnapshot stopStack(HttpSession session) {
        return crafting.stopStack(gameSession(session));
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

    @PostMapping("/api/crafting/debug")
    public CraftingService.DebugResult debug(@RequestBody DebugCommand command, HttpSession session) {
        if (command.action() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a debug action.");
        try {
            return crafting.debug(gameSession(session), command.challengeId(), command.action());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    public record DebugCommand(String challengeId, CraftingService.DebugAction action) {}

    @PostMapping("/api/crafting/debug/target")
    public CraftingService.GameSnapshot selectTarget(@RequestBody TargetCommand command, HttpSession session) {
        try {
            return crafting.selectTarget(gameSession(session), command.challengeId(), command.targetId());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    public record TargetCommand(String challengeId, String targetId) {}

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
