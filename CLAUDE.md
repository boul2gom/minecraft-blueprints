# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Minecraft Blueprints** is a Fabric mod for Minecraft 1.21.10 that recreates Unreal Engine's Blueprint visual scripting system within Minecraft. The goal is to enable players to create complex game logic through an in-game node-based visual editor, without writing any code.

This is an ambitious project combining:
- Visual programming paradigm (node graphs with execution flow)
- Real-time in-game GUI rendering
- Client-server architecture with custom networking
- Database persistence (MongoDB/SQLite)
- High-performance execution engine with safety limits

## Multi-Module Architecture

The project uses a **two-module Gradle structure**:

### 1. API Module (`API/` directory, project name: `blueprints-api`)

**Purpose**: Pure Java API for blueprint abstractions

**Location**: `API/src/main/java/fr/boul2gom/blueprints/api/`

**Key Characteristics**:
- Contains **only interfaces and enums** (no implementations)
- Uses `compileOnly` for Fabric dependencies (no runtime coupling)
- Configured as `java-library` for third-party extension
- Independent versioning (`api_version` property)

**Core Interfaces**:
- `IBlueprintNode` - Node abstraction with execute(), validate(), pin management
- `IBlueprintPin` - Connection point with type checking and connection logic
- `IBlueprintConnection` - Link between two pins
- `PinType` - Enum defining data types (EXECUTION_FLOW, BOOLEAN, INTEGER, ENTITY, etc.) with colors
- `PinDirection` - Enum for INPUT/OUTPUT
- `NodePosition` - Record for 2D canvas position (x, y)

**Why separate API?**
- Allows third-party mods to create custom nodes without depending on core implementation
- Enforces clean separation between contract and implementation
- Enables potential future extraction to standalone library

### 2. Core Module (root directory, project name: `minecraft-blueprints`)

**Purpose**: Minecraft-specific implementation

**Key Structure**:
- `src/main/java/` - Server + common code (execution engine, networking, storage)
- `src/client/java/` - Client-only code (GUI, rendering, canvas)
- Uses Fabric Loom's `splitEnvironmentSourceSets()` for client/server split

**Dependencies**:
- Minecraft 1.21.10
- Fabric API 0.136.0+1.21.10
- MongoDB driver 5.6.1
- `blueprints-api` module (via `implementation project(":blueprints-api")`)

## Build Commands

```bash
# Build entire project (both API and Core)
./gradlew build

# Generate Minecraft sources for IDE navigation (decompile Minecraft)
./gradlew genSources

# Run Minecraft client with mod loaded (for testing GUI)
./gradlew runClient

# Run Minecraft server with mod loaded (for testing server-side logic)
./gradlew runServer

# Build only API module
./gradlew :blueprints-api:build

# Clean all build artifacts
./gradlew clean
```

## Blueprint System Architecture

### Core Concepts (Unreal Engine Inspired)

**Nodes**: Executable units of logic (events, actions, flow control, pure functions)
- Have input and output pins
- Execute via `execute(ExecutionContext)` method
- Can be impure (with exec pins) or pure (data-only, evaluated on-demand)

**Pins**: Connection points on nodes
- **Execution Pins** (PinType.EXECUTION_FLOW): Control execution flow (like Unreal's white exec pins)
- **Data Pins**: Typed data inputs/outputs (BOOLEAN, INTEGER, FLOAT, ENTITY, BLOCK_POS, etc.)
- Color-coded based on type (see `PinType` enum for color values)
- Direction: INPUT (left side) or OUTPUT (right side)

**Connections**: Links between an output pin and an input pin
- Must match types (cannot connect BOOLEAN to ENTITY)
- Execution pins only connect to execution pins
- Validates direction (output → input only)

**Execution Flow**:
1. Execution starts at an **Event Node** (triggered by Fabric events)
2. Event node executes, then triggers nodes connected to its output exec pins
3. Execution flows left-to-right through exec pin connections
4. Pure nodes (no exec pins) are evaluated on-demand when their output is needed

**Blueprint Types** (planned):
- **Entity Blueprint**: Attached to entity types, handles entity lifecycle
- **Level Blueprint**: Attached to worlds, handles world-wide events
- **Function Blueprint**: Pure functions with inputs/outputs
- **Blueprint Interface**: Contract definition for inter-blueprint communication
- **Macro Library**: Reusable collapsed node groups

### Key Components

**Blueprint Workbench** (`src/main/java/fr/boul2gom/blueprints/blocks/BlueprintWorkbench.java`):
- Custom block registered in mod initializer
- Right-clicking opens `BlueprintScreen` GUI

**Blueprint Screen** (`src/client/java/fr/boul2gom/blueprints/screens/BlueprintScreen.java`):
- Client-side GUI for visual editor
- Uses `DrawContext` for rendering (may switch to OpenGL for complex operations)
- Dynamic sizing based on window dimensions (padding = 3% of screen size)
- Currently renders: background, border, debug coordinate overlay

**Blueprint Screen Handler** (`src/main/java/fr/boul2gom/blueprints/screens/BlueprintScreenHandler.java`):
- Server-side screen logic
- Handles blueprint save/load operations
- Registered as `BLUEPRINT_SCREEN_HANDLER` in mod initializer

**Execution Engine** (to be implemented):
- `ExecutionContext`: Runtime state container (variables, world, entity, call stack)
- `BlueprintExecutor`: Main execution orchestrator with safety limits
- `PerformanceMonitor`: Enforces max execution time (50ms) and max nodes (10,000)

**Networking** (to be implemented):
- Uses Fabric's `CustomPayload` system with `PacketCodec`
- Payloads: `SaveBlueprintPayload`, `LoadBlueprintPayload`, `ExecuteBlueprintPayload`
- Client sends blueprint edits to server
- Server executes blueprints and optionally syncs state back to client

**Storage** (to be implemented):
- **MongoDB/SQLite**: Persistent storage for blueprint library
- **JSON Serialization**: Graph structure (nodes, connections, positions)
- **NBT Integration**: Store blueprint ID in ItemStack for blueprint items

## Client-Server Communication Pattern

**Opening the Editor**:
1. Player right-clicks Blueprint Workbench
2. Server opens `BlueprintScreenHandler` for player
3. Client receives screen open packet, displays `BlueprintScreen`
4. Client may request blueprint data via `RequestBlueprintListPacket`

**Saving a Blueprint**:
1. Player edits blueprint in `BlueprintScreen` (client-side)
2. Player clicks "Save" button
3. Client serializes graph to JSON
4. Client sends `SaveBlueprintPayload(blueprintId, graphJson)` to server
5. Server validates and saves to database
6. Server sends confirmation packet back to client

**Executing a Blueprint**:
1. Fabric event fires on server (e.g., `UseBlockCallback.EVENT`)
2. Server queries which blueprints listen to this event
3. For each matching blueprint:
   - Create `ExecutionContext` with event data
   - Call `BlueprintExecutor.execute(graph, eventNode, context)`
   - Execute nodes following exec pin connections
   - Apply safety limits (time, node count)
4. Execution completes or times out

## Fabric Event Integration

The mod bridges Fabric events to blueprint event nodes:

**Event Registration Pattern**:
```java
// In MinecraftBlueprints.onInitialize()
UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
    if (world.isClient()) return ActionResult.PASS;

    // Find blueprints listening to "on_block_use"
    List<Blueprint> blueprints = BlueprintRegistry.getBlueprintsForEvent("on_block_use");

    for (Blueprint bp : blueprints) {
        ExecutionContext ctx = new ExecutionContext(world, player);
        ctx.setVariable("player", player);
        ctx.setVariable("blockPos", hitResult.getBlockPos());
        bp.executeEvent("on_block_use", ctx);
    }

    return ActionResult.PASS;
});
```

**Key Fabric Events to Support**:
- `UseBlockCallback.EVENT` - Player right-clicks block
- `PlayerBlockBreakEvents.BEFORE` - Player breaks block
- `ServerLivingEntityEvents.AFTER_DEATH` - Entity dies
- `ServerTickEvents.END_SERVER_TICK` - Server tick
- `ServerPlayConnectionEvents.JOIN` - Player joins server
- `AttackEntityCallback.EVENT` - Player attacks entity
- `UseItemCallback.EVENT` - Player uses item

## GUI Rendering Approach

**Canvas System** (to be implemented):
- Use `DrawContext` for 2D rendering (fills, text, textures)
- Fall back to OpenGL if operations are too complex (Bezier curves, gradients)
- Use custom TrueType font (font to be determined later)

**Rendering Layers**:
1. **Grid background** - Dots or lines with zoom-aware spacing
2. **Connections** - Bezier curves from output pins to input pins
3. **Nodes** - Boxes with header, pins, title
4. **UI Overlay** - Toolbar, palette, minimap, coordinate display

**Interaction**:
- **Pan**: Drag with middle mouse or spacebar + drag
- **Zoom**: Scroll wheel (clamp 0.25x - 2.0x)
- **Create Connection**: Click output pin, drag to input pin
- **Add Node**: Drag from palette to canvas, or right-click canvas for search
- **Delete**: Right-click node/connection for context menu

**Performance Optimization**:
- Only render nodes/connections in viewport (frustum culling)
- Batch draw calls where possible
- Cache rendered node textures if needed

## Node Categories

Nodes should be organized into categories in the palette:

1. **Events** (red) - Entry points triggered by Fabric events
2. **Flow Control** (white/grey) - Branch, Sequence, ForLoop, WhileLoop
3. **Actions** (blue) - SetBlock, SpawnEntity, GiveItem, SendMessage
4. **Variables** (green) - Get/Set variable, local/global scope
5. **Math** (cyan) - Add, Subtract, Multiply, Divide, Clamp, Lerp
6. **Comparison** (purple) - Equal, Greater, Less, And, Or, Not
7. **String** (magenta) - Concat, Format, Contains
8. **Vector** (yellow) - MakeVector, BreakVector, Distance, Normalize
9. **Minecraft** (orange) - Minecraft-specific utilities (GetBlockAt, GetEntity, etc.)

**Frequency-based Organization**:
- Most common nodes should be in top-level categories
- Less common nodes should be in subcategories (e.g., Events → Entity Events)
- Use search for quick access to rare nodes

## Performance Considerations

**Safety Limits** (enforced by `PerformanceMonitor`):
- Max execution time: **50ms per blueprint**
- Max nodes executed: **10,000 per invocation**
- Max blueprints per tick: **100 concurrent executions**

**Why These Limits?**
- Server tick is 50ms (20 TPS), blueprints must not cause lag
- 10,000 nodes prevents infinite loops from hanging server
- Limit concurrent blueprints to prevent resource exhaustion

**Optimization Strategies**:
- Validate graphs on save (detect cycles, unreachable nodes)
- Cache execution plans (pre-compute execution order)
- Reuse `ExecutionContext` objects (object pooling)
- Use connection pooling for database access
- Lazy evaluation for pure nodes (only execute when output is needed)

## Database Schema (Planned)

**MongoDB Collections** (if using MongoDB):
- `blueprints`: { id, name, type, graphJson, author, createdAt, updatedAt }
- `blueprint_executions`: { blueprintId, timestamp, executionTimeMs, nodeCount, errors }

**SQLite Tables** (if using SQLite):
- `blueprints` - Same fields as MongoDB
- `nodes` - Denormalized node data for faster queries
- `connections` - Connection records

**NBT Integration**:
- Blueprint items store `blueprintId` in NBT
- Right-clicking blueprint item opens editor (or executes if entity blueprint)

## Development Workflow

### Adding a New Node Type

1. Create node class in `src/main/java/fr/boul2gom/blueprints/nodes/{category}/`
2. Extend `BlueprintNode` (or `EventNode`, `ActionNode` base classes)
3. Define pins in constructor using `addInput()`, `addOutput()`
4. Implement `execute(ExecutionContext context)` method
5. Implement `validate()` for connection requirements
6. Register in `NodeRegistry.register("node_id", NodeClass::new)`
7. Add to palette in category (update `NodePalette` categories)
8. Test with a blueprint that uses the node

### Working with Client/Server Split

**Client-only code** goes in `src/client/java/`:
- GUI components (`BlueprintScreen`, canvas renderers)
- Input handlers (mouse, keyboard)
- Rendering utilities

**Server-only code** goes in `src/main/java/` and uses server-specific APIs:
- Blueprint execution engine
- Event handlers (Fabric events)
- Database access
- Packet handlers (server-side)

**Common code** goes in `src/main/java/` but uses only shared APIs:
- API implementations (`BlueprintNode`, `BlueprintPin`)
- Serialization/deserialization
- Validation logic

### Testing

**Manual Testing**:
1. `./gradlew runClient` - Test GUI and client-side logic
2. Create blueprint in-game, save it
3. Trigger event that should run blueprint
4. Check logs for execution trace

**Integration Testing** (future):
- Create test blueprints as JSON files in `src/test/resources/`
- Unit test execution engine with known blueprints
- Verify output matches expected behavior

## Coding Standards

**Naming Conventions**:
- Nodes: `{Name}Node.java` (e.g., `BranchNode`, `SetBlockNode`)
- Packets: `{Action}Payload.java` (e.g., `SaveBlueprintPayload`)
- Events: `On{Event}EventNode.java` (e.g., `OnBlockUseEventNode`)

**Package Structure**:
```
fr.boul2gom.blueprints/
├── MinecraftBlueprints.java (mod initializer)
├── api/ (concrete implementations of API interfaces)
│   ├── node/
│   ├── pin/
│   └── connection/
├── blocks/ (custom blocks)
├── screens/ (screen handlers)
├── nodes/ (node implementations)
│   ├── event/
│   ├── flow/
│   ├── action/
│   ├── variable/
│   └── math/
├── execution/ (execution engine)
├── storage/ (persistence)
├── network/ (packets)
└── util/ (utilities)
```

**Error Handling**:
- Use custom exceptions: `ExecutionException`, `ValidationException`, `InvalidConnectionException`
- Always validate blueprint graphs before execution
- Log errors to `MinecraftBlueprints.LOGGER`
- Return `ExecutionResult` with error messages (never crash server)

## Roadmap Context

The project follows a **10-phase roadmap** (see README.md for details):
1. ✅ Foundation & Core API (partially complete)
2. ⬜ Execution Engine
3. ⬜ Basic Node Library
4. ⬜ Client GUI - Canvas Renderer
5. ⬜ Client GUI - Node Palette & Interaction
6. ⬜ Networking & Persistence
7. ⬜ Extended Node Library
8. ⬜ Blueprint Types & Templates
9. ⬜ Performance & Optimization
10. ⬜ Polish & User Experience

**Current Status**: Phase 1 partially complete
- API interfaces defined
- Blueprint Workbench block exists
- Basic GUI screens exist (but no canvas rendering yet)
- No execution engine yet
- No networking yet
- No storage yet

**Next Priorities** (in order):
1. Implement concrete classes for `BlueprintNode`, `BlueprintPin`, `BlueprintConnection`
2. Build execution engine (`ExecutionContext`, `BlueprintExecutor`)
3. Create first event node (`OnBlockUseEventNode`)
4. Create first action node (`PrintNode` for testing)
5. Test basic execution: OnBlockUse → Print message

## Important Design Decisions

**Why MongoDB/SQLite?**
- Blueprints are complex nested structures (better suited for document DB like MongoDB)
- SQLite as fallback for simplicity and no external dependencies
- JSON serialization makes it portable between databases

**Why custom GUI instead of existing frameworks?**
- Minecraft has limited GUI support (no HTML/CSS, no web views)
- Need pixel-perfect control for node rendering and Bezier curves
- DrawContext provides enough primitives for our needs
- OpenGL fallback for advanced rendering (shaders, anti-aliasing)

**Why client/server split?**
- GUI must run on client (rendering, input handling)
- Execution must run on server (prevent cheating, authoritative logic)
- Blueprints are saved server-side (persistent across sessions)
- Client is a "thin editor" that sends changes to server

**Why Java 21?**
- Minecraft 1.21.10 requires Java 21
- Enables use of records (`NodePosition`, payloads)
- Pattern matching, switch expressions for cleaner code

## Common Pitfalls

1. **Forgetting to check world.isClient()**: Always check in event handlers!
2. **Running GUI code on server**: Use client-only source set correctly
3. **Not validating connections**: Always use `pin.canConnectTo()` before connecting
4. **Infinite loops**: Implement cycle detection in graph validation
5. **Thread safety**: ExecutionContext may be accessed from different threads
6. **NBT key names**: Use mod ID prefix to avoid conflicts (`minecraft-blueprints:blueprint_id`)

## Useful Resources

- **Fabric Wiki**: https://wiki.fabricmc.net/
- **Fabric API Javadoc**: https://maven.fabricmc.net/docs/
- **Unreal Blueprint Docs**: https://dev.epicgames.com/documentation/en-us/unreal-engine/blueprints-visual-scripting-in-unreal-engine
- **Yarn Mappings**: https://fabricmc.net/develop/ (for Minecraft source navigation)

## FAQ

**Q: Can players create malicious blueprints (lag bombs, exploits)?**
A: No. The execution engine has strict limits (50ms timeout, 10k node limit). Server validates all blueprints before execution. Players cannot execute arbitrary code, only connect existing nodes.

**Q: How do blueprints persist across server restarts?**
A: Blueprints are saved to MongoDB/SQLite database. On server start, the `BlueprintRegistry` loads all blueprints and re-registers event listeners.

**Q: Can blueprints call other blueprints?**
A: Yes (planned). Use **Blueprint Interface** to define contracts, then use **Call Blueprint** node to invoke another blueprint.

**Q: How do I add a new pin type (e.g., for my custom mod's object)?**
A: Add to `PinType` enum in API module, or use `PinTypeRegistry.register()` from your mod.

**Q: Why is the GUI so slow?**
A: Rendering optimization is Phase 9. Early versions render all nodes every frame. Later, we'll add culling, caching, and batching.

---

**Project Philosophy**: "Make visual scripting in Minecraft as powerful as Unreal's Blueprints, but accessible enough for non-programmers to create amazing gameplay."
