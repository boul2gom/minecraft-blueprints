package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tick-based scheduler for blueprint async operations.
 * Integrates with Minecraft's server tick system to schedule delayed execution.
 */
public class BlueprintScheduler implements IBlueprintScheduler {

    private final List<ScheduledTask> tasks = new ArrayList<>();

    @Override
    public CompletableFuture<Void> schedule(int ticks, Runnable task) {
        if (ticks <= 0) {
            // Execute immediately
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<Void> future = new CompletableFuture<>();
        final ScheduledTask scheduled = new ScheduledTask(ticks, task, future);

        synchronized (this.tasks) {
            this.tasks.add(scheduled);
        }

        return future;
    }

    @Override
    public void tick_scheduler() {
        final List<ScheduledTask> ready_tasks = new ArrayList<>();

        synchronized (this.tasks) {
            // Decrement all task delays and collect ready tasks
            for (final ScheduledTask task : this.tasks) {
                task.ticks_remaining--;

                if (task.ticks_remaining <= 0) {
                    ready_tasks.add(task);
                }
            }

            // Remove ready tasks from pending list
            this.tasks.removeAll(ready_tasks);
        }

        // Execute ready tasks
        for (final ScheduledTask task : ready_tasks) {
            try {
                task.runnable.run();
                task.future.complete(null);
            } catch (Exception e) {
                MinecraftBlueprints.LOGGER.error("Error executing scheduled task", e);
                task.future.completeExceptionally(e);
            }
        }
    }

    @Override
    public int pending() {
        synchronized (this.tasks) {
            return this.tasks.size();
        }
    }

    /**
     * Cancels all pending scheduled tasks.
     * This should be called during server shutdown or when clearing blueprint registry.
     * All associated CompletableFutures will be completed exceptionally.
     */
    public void cancel_all() {
        final List<ScheduledTask> to_cancel;

        synchronized (this.tasks) {
            to_cancel = new ArrayList<>(this.tasks);
            this.tasks.clear();
        }

        for (final ScheduledTask task : to_cancel) {
            task.future.completeExceptionally(
                new Exception("Task cancelled: Blueprint scheduler cleared")
            );
        }

        MinecraftBlueprints.LOGGER.info("Cancelled {} pending scheduled tasks", to_cancel.size());
    }

    @Override
    public String toString() {
        return String.format("BlueprintScheduler(pending_tasks=%d)", this.pending());
    }

    private static class ScheduledTask {
        volatile int ticks_remaining;  // Volatile ensures visibility across threads
        final Runnable runnable;
        final CompletableFuture<Void> future;

        ScheduledTask(int ticks, Runnable runnable, CompletableFuture<Void> future) {
            this.ticks_remaining = ticks;
            this.runnable = runnable;
            this.future = future;
        }
    }
}
