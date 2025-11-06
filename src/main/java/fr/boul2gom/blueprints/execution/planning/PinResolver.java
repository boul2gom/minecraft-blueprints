package fr.boul2gom.blueprints.execution.planning;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinDirection;
import fr.boul2gom.blueprints.api.execution.context.IExecutionFrame;
import fr.boul2gom.blueprints.api.execution.planning.IPinResolver;
import fr.boul2gom.blueprints.execution.context.ExecutionFrame;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provider implementation for resolving pin values during execution.
 * Uses a frame stack to isolate pin values across loop iterations.
 * Handles pure node evaluation and data flow tracing.
 */
public class PinResolver implements IPinResolver {

    private final Deque<IExecutionFrame> frame_stack;
    private final Map<IBlueprintPin, Object> pending_frame_values;
    private final Supplier<IBlueprintNode> current_node_supplier;
    private final Consumer<IBlueprintNode> current_node_setter;
    private final Function<IBlueprintNode, CompletableFuture<?>> node_executor;

    /**
     * Creates a new PinResolver with the required callbacks.
     * This eliminates two-phase initialization and ensures the resolver is always
     * fully configured before use.
     *
     * @param currentNodeSupplier Supplier to get current node
     * @param currentNodeSetter Consumer to set current node
     * @param nodeExecutor Function to execute a node and return its CompletableFuture
     */
    public PinResolver(
        Supplier<IBlueprintNode> currentNodeSupplier,
        Consumer<IBlueprintNode> currentNodeSetter,
        Function<IBlueprintNode, CompletableFuture<?>> nodeExecutor
    ) {
        this.frame_stack = new ArrayDeque<>();
        // Push root frame
        this.frame_stack.push(new ExecutionFrame());

        // Initialize pending frame values (for loop output pin isolation)
        this.pending_frame_values = new HashMap<>();

        // Store callbacks
        this.current_node_supplier = currentNodeSupplier;
        this.current_node_setter = currentNodeSetter;
        this.node_executor = nodeExecutor;
    }

    @Override
    public Optional<Object> resolve(IBlueprintPin pin) {
        if (pin.getDirection() != PinDirection.INPUT) {
            return Optional.empty();
        }

        final IExecutionFrame current_frame = this.frame_stack.peek();
        if (current_frame == null) {
            return Optional.empty();
        }

        // Check current frame cache
        if (current_frame.has_pin_value(pin)) {
            return current_frame.get_pin_value(pin);
        }

        // Find connected output pin
        if (!pin.isConnected()) {
            return Optional.empty();
        }

        final IBlueprintPin output_pin = pin.getPins().stream()
            .filter(p -> p.getDirection() == PinDirection.OUTPUT)
            .findFirst()
            .orElse(null);

        if (output_pin == null) {
            return Optional.empty();
        }

        // Check if output pin has cached value
        if (current_frame.has_pin_value(output_pin)) {
            return current_frame.get_pin_value(output_pin);
        }

        // Evaluate pure node on-demand
        final IBlueprintNode source_node = output_pin.getNode();
        if (this.isPure(source_node) && this.node_executor != null) {
            // Save current node context
            final IBlueprintNode previous_node = this.current_node_supplier != null
                ? this.current_node_supplier.get()
                : null;

            try {
                if (this.current_node_setter != null) {
                    this.current_node_setter.accept(source_node);
                }

                // Execute pure node and wait for completion
                // IMPORTANT: Pure nodes MUST return completed futures (synchronous evaluation)
                // Blocking here is acceptable since pure nodes should be fast, in-memory operations
                // If a pure node performs async I/O, it will block the executor thread
                final CompletableFuture<?> future = this.node_executor.apply(source_node);

                // Block and wait for async execution to complete
                // Pure nodes must complete before their output values can be read
                future.join();

                // Return cached value from current frame
                return current_frame.get_pin_value(output_pin);
            } finally {
                // Restore previous node (guaranteed even on exception)
                if (this.current_node_setter != null && previous_node != null) {
                    this.current_node_setter.accept(previous_node);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void set(IBlueprintPin pin, Object value) {
        final IExecutionFrame current_frame = this.frame_stack.peek();
        if (current_frame != null) {
            current_frame.set_pin_value(pin, value);
        }
    }

    @Override
    public boolean isPure(IBlueprintNode node) {
        // Check if node has any execution pins
        for (final IBlueprintPin input : node.getInputs()) {
            if (input.isExecution()) {
                return false;
            }
        }
        for (final IBlueprintPin output : node.getOutputs()) {
            if (output.isExecution()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void invalidate(IBlueprintNode node) {
        final IExecutionFrame current_frame = this.frame_stack.peek();
        if (current_frame != null) {
            current_frame.invalidate_node(node);
        }
    }

    @Override
    public void clear_cache() {
        final IExecutionFrame current_frame = this.frame_stack.peek();
        if (current_frame != null) {
            current_frame.clear();
        }
    }

    /**
     * Stores a pin value that will be applied to the next frame when push_frame() is called.
     * This is used by loop nodes to set output pin values (like index) that should be
     * accessible in the loop body frame, not the parent frame.
     *
     * Example: ForLoop sets its 'index' output pin value before the loop body frame is pushed.
     * Without this mechanism, the index would be stored in the parent frame and invisible
     * to nodes inside the loop body.
     *
     * @param pin The pin to set
     * @param value The value to store
     */
    public void set_pin_value_for_next_frame(IBlueprintPin pin, Object value) {
        this.pending_frame_values.put(pin, value);
    }

    /**
     * Pushes a new execution frame onto the stack.
     * Should be called when entering a loop body.
     * Applies any pending pin values to the new frame before pushing.
     *
     * @param owner The node creating the new frame
     */
    public void push_frame(IBlueprintNode owner) {
        final IExecutionFrame current_frame = this.frame_stack.peek();
        if (current_frame != null) {
            final IExecutionFrame child_frame = current_frame.create_child(owner);

            // Apply pending frame values to the new frame (for loop output pins)
            for (Map.Entry<IBlueprintPin, Object> entry : this.pending_frame_values.entrySet()) {
                child_frame.set_pin_value(entry.getKey(), entry.getValue());
            }
            this.pending_frame_values.clear();

            this.frame_stack.push(child_frame);
        }
    }

    /**
     * Pops the current execution frame from the stack.
     * Should be called when exiting a loop body.
     */
    public void pop_frame() {
        if (this.frame_stack.size() > 1) {
            this.frame_stack.pop();
        }
    }

    /**
     * Gets the current frame depth.
     *
     * @return The depth of the current frame
     */
    public int get_frame_depth() {
        final IExecutionFrame current_frame = this.frame_stack.peek();
        return current_frame != null ? current_frame.getDepth() : 0;
    }

    @Override
    public String toString() {
        return String.format("PinResolver(frame_depth=%d)", this.get_frame_depth());
    }
}
