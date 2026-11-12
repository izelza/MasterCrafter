package com.bursie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class StackServiceTest {
    private static CraftingService service;
    private static RecipeCatalog recipes;
    private static ItemCatalog items;

    @BeforeAll
    static void loadData() throws IOException {
        var mapper = new ObjectMapper();
        items = new ItemCatalog(mapper);
        recipes = new RecipeCatalog(mapper, items);
        service = new CraftingService(items, recipes);
    }

    @Test
    void stackHas64UniqueTargetsWithTheRequestedMinimumDifficultyMix() {
        for (int sample = 0; sample < 10; sample++) {
            var state = service.startStack(new CraftingSession());
            var entries = state.stack().entries();
            assertThat(entries).hasSize(64);
            assertThat(entries.stream().map(e -> e.target().id())).doesNotHaveDuplicates();
            assertThat(entries.stream().collect(Collectors.groupingBy(StackRun.EntryState::difficulty, Collectors.counting())))
                    .isEqualTo(Map.of(1, 19L, 2, 20L, 3, 20L, 4, 4L, 5, 1L));
            for (var entry : entries) {
                int minimum = recipes.recipesFor(entry.target().id()).stream().mapToInt(CraftingRecipe::difficulty).min().orElseThrow();
                assertThat(entry.difficulty()).isEqualTo(minimum);
            }
            for (String target : recipes.targetIds()) {
                int minimum = recipes.recipesFor(target).stream().mapToInt(CraftingRecipe::difficulty).min().orElseThrow();
                if (minimum >= 4) assertThat(entries.stream().map(e -> e.target().id())).contains(target);
            }
        }
    }

    @Test
    void all64TargetsAreSolvedInOrderWithoutSkippingOrDoubleCountingAndTimeFreezes() {
        var game = new CraftingSession();
        var start = service.startStack(game);
        assertThat(service.startStack(game).stack().id()).isEqualTo(start.stack().id());
        assertThat(service.newChallenge(game).id()).isEqualTo(start.challenge().id());
        assertThatThrownBy(() -> service.reveal(game, game.challenge().id())).isInstanceOf(IllegalStateException.class);
        assertThat(service.attempt(game, game.challenge().id(), Collections.nCopies(9, null)).correct()).isFalse();
        assertThat(service.gameState(game).stack().remaining()).isEqualTo(64);
        for (int index = 0; index < 64; index++) {
            var challenge = game.challenge();
            assertThat(challenge.target().id()).isEqualTo(start.stack().entries().get(index).target().id());
            var grid = recipes.recipesFor(challenge.target().id()).getFirst().exampleGrid();
            var correct = service.attempt(game, challenge.id(), grid);
            assertThat(correct.correct()).isTrue();
            assertThat(service.attempt(game, challenge.id(), grid)).isSameAs(correct);
            var progress = service.gameState(game).stack();
            assertThat(progress.completed()).isEqualTo(index + 1);
            assertThat(progress.remaining()).isEqualTo(63 - index);
            assertThat(progress.entries().stream().filter(StackRun.EntryState::completed)).hasSize(index + 1);
            var next = service.newChallenge(game);
            if (index < 63) {
                assertThat(next.id()).isNotEqualTo(challenge.id());
                assertThat(service.newChallenge(game)).isSameAs(next);
            } else assertThat(next.id()).isEqualTo(challenge.id());
        }
        var finish = service.gameState(game).stack();
        assertThat(finish.status()).isEqualTo(StackRun.Status.COMPLETE);
        assertThat(finish.remaining()).isZero();
        assertThat(service.gameState(game).stack().elapsedMillis()).isEqualTo(finish.elapsedMillis());
        assertThat(finish.entries()).allSatisfy(entry -> assertThat(entry.completed()).isTrue());
    }

    @Test
    void stopResetsTheTimerAndReturnsToFreePlayAndCanBeRetried() {
        var game = new CraftingSession();
        var start = service.startStack(game);
        service.attempt(game, start.challenge().id(), recipes.recipesFor(start.challenge().target().id()).getFirst().exampleGrid());
        var stopped = service.stopStack(game);
        assertThat(stopped.stack().status()).isEqualTo(StackRun.Status.INACTIVE);
        assertThat(stopped.stack().elapsedMillis()).isZero();
        assertThat(stopped.stack().entries()).isEmpty();
        assertThat(stopped.challenge().id()).isNotEqualTo(start.challenge().id());
        assertThat(service.stopStack(game).challenge().id()).isEqualTo(stopped.challenge().id());
        assertThat(service.reveal(game, stopped.challenge().id()).grid()).hasSize(9);
        var restarted = service.startStack(game);
        assertThat(restarted.stack().id()).isNotEqualTo(start.stack().id());
        assertThat(restarted.stack().completed()).isZero();
        assertThatThrownBy(() -> service.attempt(game, start.challenge().id(), Collections.nCopies(9, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playersHaveIndependentStacksAndFreePlayStillWorks() {
        var first = new CraftingSession();
        var second = new CraftingSession();
        service.startStack(first);
        assertThat(service.gameState(second).stack().status()).isEqualTo(StackRun.Status.INACTIVE);
        var secondStart = service.startStack(second);
        service.stopStack(first);
        assertThat(service.gameState(second).stack().id()).isEqualTo(secondStart.stack().id());
        assertThat(service.gameState(second).stack().status()).isEqualTo(StackRun.Status.RUNNING);
    }

    @Test
    void stopwatchUsesElapsedTimeAndFreezesAtTheFinalSolve() {
        var nanos = new AtomicLong(1_000_000_000L);
        var run = new StackRun(List.of(new StackRun.Entry(items.find("minecraft:stick"), 1),
                new StackRun.Entry(items.find("minecraft:crafting_table"), 1)), nanos::get);
        nanos.addAndGet(2_500_000_000L);
        assertThat(run.snapshot().elapsedMillis()).isEqualTo(2500);
        run.markSolved("minecraft:stick");
        nanos.addAndGet(1_250_000_000L);
        run.markSolved("minecraft:crafting_table");
        assertThat(run.snapshot().elapsedMillis()).isEqualTo(3750);
        nanos.addAndGet(99_000_000_000L);
        assertThat(run.snapshot().elapsedMillis()).isEqualTo(3750);
        assertThat(run.snapshot().status()).isEqualTo(StackRun.Status.COMPLETE);
    }
}
