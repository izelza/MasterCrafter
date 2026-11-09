package com.bursie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Spellogiken kan testas utan HTTP, MockMvc eller en startad Spring-applikation. */
class CraftingServiceTest {
    private static CraftingService service;
    private static ItemCatalog items;

    @BeforeAll
    static void loadMinecraftData() throws IOException {
        var mapper = new ObjectMapper();
        items = new ItemCatalog(mapper);
        service = new CraftingService(items, new RecipeCatalog(mapper, items));
    }

    @Test
    void sessionTracksWrongAttemptsSolvedStateAndSafeRetries() {
        var game = new CraftingSession();
        assertThat(game.status()).isEqualTo(CraftingSession.Status.NOT_STARTED);
        assertThatThrownBy(() -> service.attempt(game, "missing", blank()))
                .isInstanceOf(IllegalStateException.class);

        game.start(stickChallenge("stick-question"));
        var wrong = service.attempt(game, "stick-question", blank());
        assertThat(wrong.correct()).isFalse();
        assertThat(game.status()).isEqualTo(CraftingSession.Status.ACTIVE);
        assertThat(service.attempt(game, "stick-question", blank())).isSameAs(wrong);

        var correct = service.attempt(game, "stick-question", stickGrid());
        assertThat(correct.correct()).isTrue();
        assertThat(game.status()).isEqualTo(CraftingSession.Status.SOLVED);
        assertThat(service.attempt(game, "stick-question", stickGrid())).isSameAs(correct);
        assertThatThrownBy(() -> service.attempt(game, "stick-question", blank()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(game.status()).isEqualTo(CraftingSession.Status.SOLVED);
    }

    @Test
    void cachedGridIsASnapshotAndStartingNextChallengeResetsProgress() {
        var game = new CraftingSession();
        game.start(stickChallenge("old-question"));
        var submitted = blank();
        var wrong = service.attempt(game, "old-question", submitted);
        submitted.set(0, "minecraft:diamond");
        assertThat(service.attempt(game, "old-question", blank())).isSameAs(wrong);
        service.attempt(game, "old-question", stickGrid());

        var next = service.newChallenge(game);
        assertThat(next).isSameAs(game.challenge());
        assertThat(next.id()).isNotEqualTo("old-question");
        assertThat(next.target().id()).isNotEqualTo("minecraft:stick");
        assertThat(game.status()).isEqualTo(CraftingSession.Status.ACTIVE);
        assertThat(service.attempt(game, next.id(), blank())).isNotSameAs(wrong);
        assertThatThrownBy(() -> service.attempt(game, "old-question", stickGrid()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoPlayersKeepIndependentProgress() {
        var first = new CraftingSession();
        var second = new CraftingSession();
        first.start(stickChallenge("first-player"));
        second.start(stickChallenge("second-player"));
        service.attempt(first, "first-player", stickGrid());
        assertThat(first.status()).isEqualTo(CraftingSession.Status.SOLVED);
        assertThat(second.status()).isEqualTo(CraftingSession.Status.ACTIVE);
        assertThatThrownBy(() -> service.attempt(second, "first-player", stickGrid()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(service.attempt(second, "second-player", stickGrid()).correct()).isTrue();
    }

    private static CraftingSession.Challenge stickChallenge(String id) {
        return new CraftingSession.Challenge(id, items.find("minecraft:stick"));
    }

    private static ArrayList<String> blank() {
        return new ArrayList<>(Collections.nCopies(9, null));
    }

    private static java.util.List<String> stickGrid() {
        return Arrays.asList("minecraft:oak_planks", null, null,
                "minecraft:oak_planks", null, null, null, null, null);
    }
}
