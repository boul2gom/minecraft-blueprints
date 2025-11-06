package fr.boul2gom.blueprints.api.execution;

import fr.boul2gom.blueprints.api.provider.ProviderRegistry;

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

    IBlueprintScheduler INSTANCE = ProviderRegistry.get(IBlueprintScheduler.class);

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
     * Processes the scheduled tasks for the current tick.
     * This method is called on every server tick to execute tasks that are scheduled
     * to run during the current tick or to update the state of pending tasks.
     *
     * Implementation details:
     * - Executes tasks that are due for the current tick.
     * - Updates internal state or queues for scheduling future tasks.
     * - Ensures that tasks are executed in the same order they were scheduled.
     *
     * Thread safety:
     * - Must be called on the server thread to prevent concurrent modification issues.
     *
     * Common usage:
     * - Automatically called by the system managing the tick lifecycle.
     * - Provides core functionality for tick-based scheduling of tasks.
     */
    void tick_scheduler();

    /**
     * Returns the number of tasks currently scheduled.
     * Useful for debugging and monitoring.
     *
     * @return the number of pending scheduled tasks
     */
    int pending();
}
