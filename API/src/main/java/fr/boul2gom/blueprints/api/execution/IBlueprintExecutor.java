package fr.boul2gom.blueprints.api.execution;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;

import java.time.Duration;

/**
 * The IBlueprintExecutor interface defines the contract for executing blueprint graphs.
 * It provides default execution constraints, including maximum execution time and
 * maximum nodes that can be processed during execution.
 *
 * The interface allows the execution of blueprint graphs with a provided context,
 * and it returns an execution result detailing the success, failure, or other status
 * information regarding the execution.
 *
 * Constants:
 * - MAX_EXECUTION_TIME: The maximum allowed time for execution (default: 50ms).
 * - MAX_NODES_PER_EXECUTION: The maximum number of nodes that can be executed in a single graph run (default: 10,000).
 * - MAX_ITERATIONS_PER_LOOP: The maximum number of iterations per loop node (default: 10,000).
 *
 * Primary Responsibilities:
 * - Executing blueprint graphs with a given execution context.
 * - Adhering to pre-defined execution constraints such as the time limit and node limit.
 */
@FunctionalInterface
public interface IBlueprintExecutor {

    /** Maximum execution time */
    Duration MAX_EXECUTION_TIME = Duration.ofMillis(50);
    /** Maximum number of nodes that can be processed during execution */
    int MAX_NODES_PER_EXECUTION = 10000;
    /** Maximum number of iterations per loop node */
    int MAX_ITERATIONS_PER_LOOP = 10000;

    /**
     * Executes the given blueprint graph within the context of the provided execution environment.
     * This method processes nodes within the graph, respecting execution constraints such as
     * time limits and node count limits, as defined by the execution context.
     *
     * @param graph   The blueprint graph to be executed. Must be a valid and well-formed graph.
     * @param context The execution context containing runtime variables, environmental information,
     *                and execution-specific constraints and states.
     * @return An {@code IExecutionResult} representing the outcome of the graph execution, including
     *         success status, execution time, number of processed nodes, and error details if any.
     */
    IExecutionResult execute(IBlueprintGraph graph, IExecutionContext context);
}
