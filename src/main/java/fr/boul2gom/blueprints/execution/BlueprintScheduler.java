package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tick-based scheduler for blueprint async operations.
 * Integrates with Minecraft's server tick system to schedule delayed execution.
 */
public class BlueprintScheduler implements IBlueprintScheduler {

    private static BlueprintScheduler instance;

    private final List<ScheduledTask> tasks;

    private BlueprintScheduler() {
        this.tasks = new ArrayList<>();
    }

    public static BlueprintScheduler getInstance() {
        if (instance == null) {
            instance = new BlueprintScheduler();
            instance.register_tick_handler();
        }
        return instance;
    }

    private void register_tick_handler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            this.tick();
        });
    }

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
    public int pending() {
        synchronized (this.tasks) {
            return this.tasks.size();
        }
    }

    private void tick() {
        final List<ScheduledTask> ready_tasks;

        synchronized (this.tasks) {
            // Decrement all task delays and collect ready tasks
            ready_tasks = this.tasks.stream()
                .peek(task -> task.ticks_remaining--)
                .filter(task -> task.ticks_remaining <= 0)
                .toList();

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

    private static class ScheduledTask {
        int ticks_remaining;
        final Runnable runnable;
        final CompletableFuture<Void> future;

        ScheduledTask(int ticks, Runnable runnable, CompletableFuture<Void> future) {
            this.ticks_remaining = ticks;
            this.runnable = runnable;
            this.future = future;
        }
    }
}
