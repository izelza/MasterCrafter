package com.bursie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** En spelares crafting-status. Oberoende av HTTP och Spring. */
public class CraftingSession {
    public enum Status { NOT_STARTED, ACTIVE, SOLVED }

    private Challenge challenge;
    private Status status = Status.NOT_STARTED;
    private List<String> lastGrid;
    private AttemptResult lastResult;

    public Challenge challenge() {
        return challenge;
    }

    public Status status() {
        return status;
    }

    void start(Challenge challenge) {
        this.challenge = challenge;
        status = Status.ACTIVE;
        lastGrid = null;
        lastResult = null;
    }

    AttemptResult resultFor(List<String> grid) {
        return grid.equals(lastGrid) ? lastResult : null;
    }

    void recordAttempt(List<String> grid, AttemptResult result) {
        // Tomma rutor är null, så List.copyOf kan inte användas här.
        lastGrid = Collections.unmodifiableList(new ArrayList<>(grid));
        lastResult = result;
        if (result.correct()) status = Status.SOLVED;
    }

    public record Challenge(String id, ItemCatalog.Item target) {}
    public record AttemptResult(boolean correct, String message) {}
}
