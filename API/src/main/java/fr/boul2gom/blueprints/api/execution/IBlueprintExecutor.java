package fr.boul2gom.blueprints.api.execution;

import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;

public interface IBlueprintExecutor {

    // Execute a blueprint graph with given context
    IExecutionResult execute(IBlueprintGraph graph, IExecutionContext context);

    // Get maximum execution time in milliseconds
    long getMaxExecutionTime();

    // Get maximum number of nodes per execution
    int getMaxNodesPerExecution();
}
