package com.bursie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** One player's ordered checklist and monotonic stopwatch. Access is locked by CraftingService. */
public final class StackRun {
    public enum Status { INACTIVE, RUNNING, COMPLETE }
    public record Entry(ItemCatalog.Item target, int difficulty) {}
    public record EntryState(ItemCatalog.Item target, int difficulty, boolean completed, boolean current) {}
    public record Snapshot(String id, Status status, int total, int completed, int remaining,
                           long elapsedMillis, List<EntryState> entries) {
        static Snapshot inactive() {
            return new Snapshot(null, Status.INACTIVE, 0, 0, 0, 0, List.of());
        }
    }

    private final String id = UUID.randomUUID().toString();
    private final List<Entry> entries;
    private final LongSupplier nanoTime;
    private final long startedAt;
    private long finishedAt;
    private int completed;

    StackRun(List<Entry> entries) {
        this(entries, System::nanoTime);
    }

    StackRun(List<Entry> entries, LongSupplier nanoTime) {
        if (entries.isEmpty() || entries.stream().map(e -> e.target().id()).distinct().count() != entries.size()) {
            throw new IllegalArgumentException("The Stack needs a nonempty list of unique targets.");
        }
        this.entries = List.copyOf(entries);
        this.nanoTime = nanoTime;
        startedAt = nanoTime.getAsLong();
    }

    public boolean running() {
        return completed < entries.size();
    }

    Entry current() {
        if (!running()) throw new IllegalStateException("The Stack is complete.");
        return entries.get(completed);
    }

    void markSolved(String targetId) {
        if (!current().target().id().equals(targetId)) throw new IllegalStateException("This is not the current stack target.");
        completed++;
        if (!running()) finishedAt = nanoTime.getAsLong();
    }

    Snapshot snapshot() {
        var checklist = new ArrayList<EntryState>();
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.get(index);
            checklist.add(new EntryState(entry.target(), entry.difficulty(), index < completed, running() && index == completed));
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis((running() ? nanoTime.getAsLong() : finishedAt) - startedAt);
        return new Snapshot(id, running() ? Status.RUNNING : Status.COMPLETE, entries.size(), completed,
                entries.size() - completed, Math.max(0, elapsed), List.copyOf(checklist));
    }
}
