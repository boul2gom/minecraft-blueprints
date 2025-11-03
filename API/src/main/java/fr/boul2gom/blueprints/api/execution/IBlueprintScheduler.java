package fr.boul2gom.blueprints.api.execution;

import java.util.concurrent.CompletableFuture;

/**
 * The IBlueprintScheduler interface provides tick-based scheduling for blueprint execution.
 * It allows nodes to schedule delayed execution using Minecraft's tick system.
 *
 * Use cases:
 * - DelayNode: Schedule execution continuation after N ticks
 * - Async operations: Wait for Minecraft tick to complete before continuing
 * - Rate limiting: Spread heavy operations across multiple ticks
 *
 * Implementation notes:
 * - Scheduler runs on server thread (safe for Minecraft API calls)
 * - Delays are specified in ticks (20 ticks = 1 second)
 * - Scheduled tasks return CompletableFuture for async chaining
 */
public interface IBlueprintScheduler {

    /**
     * Schedules a task to run after the specified number of ticks.
     *
     * @param ticks number of ticks to wait (20 ticks = 1 second)
     * @param task the runnable to execute after the delay
     * @return a CompletableFuture that completes when the task executes
     */
    CompletableFuture<Void> schedule(int ticks, Runnable task);

    /**
     * Schedules a task to run on the next server tick.
     * Equivalent to schedule(1, task).
     *
     * @param task the runnable to execute
     * @return a CompletableFuture that completes when the task executes
     */
    default CompletableFuture<Void> schedule_next_tick(Runnable task) {
        return this.schedule(1, task);
    }

    /**
     * Returns the number of tasks currently scheduled.
     * Useful for debugging and monitoring.
     *
     * @return the number of pending scheduled tasks
     */
    int pending();
}
