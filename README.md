# 🎮 Minecraft Blueprints

> **Unreal Engine-style Visual Scripting System for Minecraft 1.21.10**

A comprehensive Fabric mod that brings the power of Unreal Engine's Blueprint visual scripting system into Minecraft, enabling players and creators to build complex game logic without writing a single line of code.

---

## 📑 Table of Contents

- [Introduction](#-introduction)
- [Architecture](#-architecture)
- [Roadmap](#-roadmap)
- [Core Classes](#-core-classes)
- [Event System](#-event-system)
- [Blueprint Nodes](#-blueprint-nodes)
- [Technical Integration](#-technical-integration)
- [Development Guide](#-development-guide)

---

## 🎯 Introduction

### Project Vision

**Minecraft Blueprints** recreates Unreal Engine's renowned visual scripting system within Minecraft, democratizing game logic creation. Instead of requiring Java knowledge, players can build sophisticated behaviors through an intuitive node-based interface.

### Unreal Engine Blueprints vs. Minecraft Blueprints

| Feature | Unreal Engine | Minecraft Blueprints |
|---------|---------------|---------------------|
| **Visual Scripting** | ✅ Node-based graph editor | ✅ In-game canvas editor |
| **Execution Flow** | ✅ White exec pins, left-to-right | ✅ Execution flow pins |
| **Data Types** | ✅ Typed pins (int, float, object, etc.) | ✅ Minecraft-specific types (Entity, BlockPos, ItemStack) |
| **Events** | ✅ BeginPlay, Tick, Input events | ✅ Fabric events (UseBlock, EntityDeath, ServerTick) |
| **Blueprint Types** | ✅ Actor, Level, Interface, Macro, Function | ✅ Entity, Level, Interface, Macro, Function Library |
| **Hot Reload** | ✅ Compile and reload | ✅ In-game save and execute |
| **Environment** | Desktop Editor | **In-Game GUI** |

### Core Concepts Adapted from Unreal

- **Entity Blueprints**: Define custom entity behaviors and AI logic
- **Level Blueprints**: World-specific scripts triggered by player actions
- **Blueprint Interfaces**: Contracts for communication between blueprints
- **Macro Libraries**: Reusable node clusters with collapse/expand
- **Function Libraries**: Pure functions for math, utilities, and helpers
- **Event-Driven Execution**: Start from events, flow through logic, execute actions

---

## 🏗️ Architecture

### Multi-Module Structure

```
minecraft-blueprints/
├── API/                          # Pure Java API module
│   ├── src/main/java/
│   │   └── fr/boul2gom/blueprints/api/
│   │       ├── node/
│   │       │   ├── IBlueprintNode.java
│   │       │   └── NodePosition.java
│   │       ├── pin/
│   │       │   ├── IBlueprintPin.java
│   │       │   ├── PinType.java
│   │       │   └── PinDirection.java
│   │       ├── connection/
│   │       │   └── IBlueprintConnection.java
│   │       └── BlueprintsAPI.java
│   └── build.gradle              # API module config
│
├── src/                          # Core implementation
│   ├── main/java/                # Server + Common code
│   │   └── fr/boul2gom/blueprints/
│   │       ├── MinecraftBlueprints.java
│   │       ├── blocks/
│   │       │   └── BlueprintWorkbench.java
│   │       ├── screens/
│   │       │   └── BlueprintScreenHandler.java
│   │       ├── api/              # Concrete implementations
│   │       │   ├── node/
│   │       │   ├── pin/
│   │       │   └── connection/
│   │       ├── execution/        # Execution engine
│   │       ├── storage/          # Persistence layer
│   │       └── network/          # Client-server packets
│   │
│   ├── client/java/              # Client-only code
│   │   └── fr/boul2gom/blueprints/
│   │       ├── MinecraftBlueprintsClient.java
│   │       ├── screens/
│   │       │   └── BlueprintScreen.java
│   │       └── rendering/
│   │           ├── canvas/       # Node canvas renderer
│   │           └── ui/           # UI components
│   │
│   └── main/resources/
│       ├── fabric.mod.json
│       └── assets/minecraft-blueprints/
│
└── build.gradle                  # Root project config
```

### API/Core Separation Rationale

**API Module** (`blueprints-api`):
- Contains **only interfaces and enums**
- No Minecraft dependencies (uses `compileOnly`)
- Allows third-party mods to extend blueprint functionality
- Versioned independently (`api_version`)

**Core Module** (`minecraft-blueprints`):
- Implements all API interfaces
- Contains Minecraft-specific logic (blocks, entities, GUI)
- Depends on API module
- Handles execution, networking, and persistence

### Client-Server Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT SIDE                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BlueprintScreen (GUI)                                     │  │
│  │  ├─ Canvas Renderer (nodes, connections, grid)           │  │
│  │  ├─ Node Palette (categorized drag-drop)                 │  │
│  │  ├─ Toolbar (save, load, execute, debug)                 │  │
│  │  └─ Input Handlers (mouse, keyboard, zoom/pan)           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Client Networking (send packets to server)               │  │
│  │  ├─ SaveBlueprintPacket                                  │  │
│  │  ├─ ExecuteBlueprintPacket                               │  │
│  │  └─ RequestBlueprintListPacket                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ▲ │
                              │ ▼
                     [Network Layer]
                              ▲ │
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                         SERVER SIDE                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Server Networking (receive packets from client)          │  │
│  │  ├─ Handle SaveBlueprintPacket                           │  │
│  │  ├─ Handle ExecuteBlueprintPacket                        │  │
│  │  └─ Send BlueprintListPacket                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BlueprintExecutionEngine                                 │  │
│  │  ├─ Event Registry (map events to blueprints)           │  │
│  │  ├─ Execution Context (runtime state)                    │  │
│  │  ├─ Node Executor (traverse graph, execute nodes)        │  │
│  │  └─ Performance Monitor (tick limits, safety checks)     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Storage Layer                                            │  │
│  │  ├─ MongoDB/SQLite (blueprint data)                      │  │
│  │  ├─ JSON Serializer (graph structure)                    │  │
│  │  └─ NBT Integration (blueprint item IDs)                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Gradle Configuration

**Root `build.gradle`:**
```gradle
plugins {
    id 'fabric-loom' version '1.11-SNAPSHOT'
    id 'java'
}

version = project.mod_version
group = project.maven_group

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"

    // API module dependency
    implementation project(":blueprints-api")

    // Database support
    implementation platform("org.mongodb:mongodb-driver-bom:5.6.1")
    implementation "org.mongodb:mongodb-driver-sync"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

**API `build.gradle`:**
```gradle
plugins {
    id 'fabric-loom' version '1.11-SNAPSHOT'
    id 'java-library'
}

version = project.api_version
group = project.maven_group

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"

    compileOnly "net.fabricmc:fabric-loader:${project.loader_version}"
    modCompileOnly "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}
```

---

## 🗺️ Roadmap

### Phase 1: Foundation & Core API 🎯 (80% Complete - 2 items remaining)

**Objectives:**
- Define core abstractions for nodes, pins, and connections
- Establish multi-module architecture
- Create basic workbench block

**Deliverables:**
- ✅ `IBlueprintNode`, `IBlueprintPin`, `IBlueprintConnection` interfaces
- ✅ `PinType` enum with 12 Minecraft-specific types and colors
- ✅ Blueprint Workbench block with multi-block structure and GUI trigger
- ✅ `BlueprintPin` concrete class with production-ready validation
- ✅ `BlueprintConnection` concrete class
- ✅ `BlueprintNode` abstract class with Factory Pattern
- ✅ `NodeConfig` fluent API, `NodeFactory` interface, `PinDefinition` record

**Checklist:**
- [x] Create API module structure
- [x] Define `IBlueprintNode` with execute(), validate(), getInputs(), getOutputs()
- [x] Define `IBlueprintPin` with connection management
- [x] Define `PinType` enum with colors (EXECUTION_FLOW, BOOLEAN, INTEGER, ENTITY, etc.)
- [x] Define `PinDirection` enum (INPUT, OUTPUT)
- [x] Create Blueprint Workbench block
- [x] Implement `BlueprintPin` with type checking and connection validation ⭐
- [x] Implement `BlueprintConnection` to link pins
- [x] Implement `BlueprintNode` abstract class with **Factory Pattern** ⭐

---

### Phase 2: Execution Engine ✅ (100% Complete)

**Objectives:**
- Build the runtime execution system
- Implement execution flow traversal
- Create execution context for runtime state

**Deliverables:**
- ✅ Execution engine that traverses node graphs
- ✅ Context system for storing runtime variables
- ✅ Comprehensive error handling and validation
- ✅ Performance monitoring with safety limits
- ✅ Cycle detection to prevent infinite loops
- ✅ Debug logging system for execution tracing

**Checklist:**
- [x] Create `ExecutionContext` class to store runtime variables and execution state ⭐
- [x] Implement execution queue (BFS traversal of graph) ⭐
- [x] Add execution flow following: when a node executes, queue all nodes connected to its output exec pins ⭐
- [x] Implement `BlueprintExecutor` with safety limits (max nodes per execution, timeout) ⭐
- [x] Add variable storage in context (set/get variables by name) via `VariableRegistry` ⭐
- [x] Create `ExecutionResult` class for success/failure tracking ⭐
- [x] Implement cycle detection to prevent infinite loops via `CycleDetector` ⭐
- [x] Add debug logging (which nodes executed, in what order) via `ExecutionLogger` ⭐
- [x] Create execution exceptions for runtime errors (`ExecutionException`, `ExecutionTimeoutException`, `NodeLimitExceededException`) ⭐

---

### Phase 3: Basic Node Library

**Objectives:**
- Implement fundamental node types
- Create event nodes for Fabric events
- Build flow control nodes

**Deliverables:**
- Event nodes (OnBlockUse, OnEntityDeath, OnServerTick)
- Flow control nodes (Branch, Sequence, ForLoop)
- Basic action nodes (Print, Delay)

**Checklist:**
- [ ] Create `EventNode` base class with automatic Fabric event registration
- [ ] Implement `OnBlockUseEventNode` (fires when player right-clicks block)
- [ ] Implement `OnEntityDeathEventNode` (fires when entity dies)
- [ ] Implement `OnServerTickEventNode` (fires every server tick)
- [ ] Create `BranchNode` (if-else logic)
- [ ] Create `SequenceNode` (execute multiple exec outputs in order)
- [ ] Create `ForLoopNode` (repeat N times with index output)
- [ ] Create `PrintNode` (log to chat/console)
- [ ] Create `DelayNode` (schedule execution after X ticks)
- [ ] Add node factory/registry for instantiation by ID
- [ ] Implement node validation (e.g., Branch requires boolean input)

---

### Phase 4: Client GUI - Canvas Renderer

**Objectives:**
- Build the in-game visual editor
- Implement node rendering and connections
- Add zoom, pan, and grid

**Deliverables:**
- Canvas rendering system using DrawContext
- Node boxes with pins rendered
- Bezier curves for connections
- Grid background with zoom/pan

**Key Classes:**

```java
// rendering/canvas/BlueprintCanvas.java
public class BlueprintCanvas {
    private float zoom = 1.0f;
    private int panX = 0, panY = 0;
    private final List<CanvasNode> nodes = new ArrayList<>();
    private final List<CanvasConnection> connections = new ArrayList<>();

    public void render(DrawContext context, int mouseX, int mouseY) {
        renderGrid(context);
        renderConnections(context);
        renderNodes(context);
        renderDraggedConnection(context, mouseX, mouseY);
    }

    private void renderConnections(DrawContext context) {
        for (CanvasConnection conn : connections) {
            drawBezierCurve(context, conn.startPin, conn.endPin, conn.color);
        }
    }
}
```

```java
// rendering/canvas/NodeRenderer.java
public class NodeRenderer {
    public static void renderNode(DrawContext context, CanvasNode node, int x, int y) {
        // Draw node box
        context.fill(x, y, x + node.width, y + node.height, 0xE6000000);

        // Draw header
        context.fill(x, y, x + node.width, y + 20, node.getHeaderColor());
        context.drawText(textRenderer, node.getName(), x + 5, y + 6, 0xFFFFFF, true);

        // Draw pins
        renderPins(context, node.getInputs(), x, y + 25, true);
        renderPins(context, node.getOutputs(), x + node.width, y + 25, false);
    }
}
```

**Checklist:**
- [ ] Create `BlueprintCanvas` class to manage canvas state (nodes, connections, pan, zoom)
- [ ] Implement grid rendering with adjustable size
- [ ] Implement node box rendering (background, header, title)
- [ ] Render pins as circles on node sides (inputs left, outputs right)
- [ ] Color pins based on `PinType.getColor()`
- [ ] Implement Bezier curve drawing for connections (use cubic Bezier for smooth curves)
- [ ] Add mouse interaction: detect which node/pin is under cursor
- [ ] Implement pan (drag with middle mouse or spacebar+drag)
- [ ] Implement zoom (scroll wheel, clamp between 0.25x - 2.0x)
- [ ] Add connection dragging preview (start from output pin, follow mouse)
- [ ] Use custom font rendering (TrueType font loaded from resources)
- [ ] Optimize rendering (only draw nodes/connections in viewport)

---

### Phase 5: Client GUI - Node Palette & Interaction

**Objectives:**
- Create node palette with categories
- Implement drag-and-drop node creation
- Add connection creation/deletion

**Deliverables:**
- Searchable node palette
- Drag nodes from palette to canvas
- Click pins to create connections
- Right-click to delete nodes/connections

**Key Classes:**

```java
// ui/NodePalette.java
public class NodePalette {
    private final Map<String, List<NodeDefinition>> categories = new LinkedHashMap<>();
    private String searchQuery = "";

    public NodePalette() {
        categories.put("Events", Arrays.asList(/* event nodes */));
        categories.put("Flow Control", Arrays.asList(/* flow nodes */));
        categories.put("Actions", Arrays.asList(/* action nodes */));
        categories.put("Variables", Arrays.asList(/* variable nodes */));
    }

    public void render(DrawContext context, int x, int y, int width, int height) {
        // Render category tabs and filtered node list
    }
}
```

**Checklist:**
- [ ] Create `NodePalette` UI component
- [ ] Organize nodes into categories (Events, Flow Control, Actions, Variables, Math, etc.)
- [ ] Implement collapsible category sections
- [ ] Add search bar with real-time filtering
- [ ] Implement drag-and-drop: drag node from palette to canvas to instantiate
- [ ] Add click-to-create connections: click output pin, then input pin
- [ ] Validate connections before creating (type compatibility, no cycles in data flow)
- [ ] Implement connection deletion (right-click connection)
- [ ] Implement node deletion (right-click node, confirm dialog)
- [ ] Add context menu on canvas (right-click empty area to search/add nodes)
- [ ] Implement multi-select (drag rectangle to select multiple nodes)
- [ ] Add copy-paste functionality

---

### Phase 6: Networking & Persistence

**Objectives:**
- Implement client-server communication
- Build save/load system with JSON
- Integrate MongoDB/SQLite storage

**Deliverables:**
- Custom packets for blueprint CRUD operations
- JSON serialization for graph structure
- Database integration for blueprint library
- NBT storage for blueprint items

**Key Classes:**

```java
// network/packets/SaveBlueprintPayload.java
public record SaveBlueprintPayload(String blueprintId, String graphJson)
    implements CustomPayload {
    public static final Id<SaveBlueprintPayload> ID =
        new Id<>(Identifier.of("minecraft-blueprints", "save_blueprint"));
    public static final PacketCodec<RegistryByteBuf, SaveBlueprintPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING, SaveBlueprintPayload::blueprintId,
            PacketCodecs.STRING, SaveBlueprintPayload::graphJson,
            SaveBlueprintPayload::new
        );
}
```

```java
// storage/BlueprintSerializer.java
public class BlueprintSerializer {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(IBlueprintNode.class, new NodeAdapter())
        .create();

    public static String serialize(BlueprintGraph graph) {
        return GSON.toJson(graph);
    }

    public static BlueprintGraph deserialize(String json) {
        return GSON.fromJson(json, BlueprintGraph.class);
    }
}
```

**Checklist:**
- [ ] Define packet payloads: `SaveBlueprintPayload`, `LoadBlueprintPayload`, `ExecuteBlueprintPayload`, `DeleteBlueprintPayload`
- [ ] Register payloads in mod initializer
- [ ] Implement server-side packet handlers (save to database, load from database)
- [ ] Implement client-side packet handlers (receive blueprint data, update GUI)
- [ ] Create `BlueprintSerializer` with Gson adapters for polymorphic nodes
- [ ] Serialize graph structure: nodes (id, type, position, pin values), connections (from/to pins)
- [ ] Implement MongoDB storage (optional): collections for blueprints, users, metadata
- [ ] Implement SQLite storage (fallback): tables for blueprints, nodes, connections
- [ ] Add NBT integration: store blueprint ID in item stack NBT
- [ ] Create blueprint item that stores reference to saved blueprint
- [ ] Add auto-save functionality (save on GUI close, periodic backup)

---

### Phase 7: Extended Node Library

**Objectives:**
- Expand node library with Minecraft-specific actions
- Add math, comparison, and variable nodes
- Implement getter/setter nodes

**Deliverables:**
- 50+ nodes covering common Minecraft operations
- Variable get/set nodes
- Math operations (add, subtract, multiply, divide)
- Minecraft actions (spawn entity, set block, give item)

**Node Categories:**

```java
// nodes/action/SetBlockNode.java
public class SetBlockNode extends ActionNode {
    public SetBlockNode() {
        super("set_block", "Set Block");
        addInput(PinType.EXECUTION_FLOW, "exec");
        addInput(PinType.WORLD_REF, "world");
        addInput(PinType.BLOCK_POS, "pos");
        addInput(PinType.BLOCK_STATE, "blockState");
        addOutput(PinType.EXECUTION_FLOW, "then");
    }

    @Override
    public void execute(ExecutionContext context) {
        World world = context.getPinValue("world", World.class);
        BlockPos pos = context.getPinValue("pos", BlockPos.class);
        BlockState state = context.getPinValue("blockState", BlockState.class);
        world.setBlockState(pos, state);
        context.executeConnectedNodes(this, "then");
    }
}
```

**Checklist:**
- [ ] **Events**: OnPlayerJoin, OnPlayerLeave, OnBlockBreak, OnBlockPlace, OnItemUse, OnCraftItem, OnAttackEntity
- [ ] **Flow Control**: While, DoOnce, DoN, Flip-Flop, Gate, MultiGate
- [ ] **Actions**: SetBlock, SpawnEntity, GiveItem, TeleportEntity, PlaySound, SendMessage, KillEntity
- [ ] **Variables**: GetVariable, SetVariable, Increment, Decrement
- [ ] **Math**: Add, Subtract, Multiply, Divide, Modulo, Power, Clamp, Lerp
- [ ] **Comparison**: Equal, NotEqual, Greater, Less, GreaterOrEqual, LessOrEqual
- [ ] **Logic**: And, Or, Not, Xor
- [ ] **String**: Concat, Format, Contains, Split
- [ ] **Vector**: MakeVector, BreakVector, VectorLength, Normalize, Distance
- [ ] **Conversion**: ToString, ToInt, ToFloat, ToBoolean

---

### Phase 8: Blueprint Types & Templates

**Objectives:**
- Implement different blueprint types (Entity, Level, Function)
- Create blueprint interface system
- Build macro library support

**Deliverables:**
- Entity Blueprint (attach to entity types)
- Level Blueprint (attach to worlds)
- Function Blueprint (pure functions, no state)
- Blueprint Interface (define contracts)
- Macro Library (reusable node groups)

**Key Classes:**

```java
// blueprint/EntityBlueprint.java
public class EntityBlueprint extends Blueprint {
    private final EntityType<?> entityType;

    @Override
    public void registerEvents() {
        // Register entity-specific events
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, world) -> {
            if (entity.getType() == entityType) {
                executeEvent("OnDeath", entity, world);
            }
        });
    }
}
```

```java
// blueprint/BlueprintInterface.java
public class BlueprintInterface {
    private final String name;
    private final List<FunctionSignature> functions = new ArrayList<>();

    public void addFunction(String name, List<PinType> inputs, List<PinType> outputs) {
        functions.add(new FunctionSignature(name, inputs, outputs));
    }
}
```

**Checklist:**
- [ ] Create `Blueprint` base class with metadata (name, type, author, version)
- [ ] Implement `EntityBlueprint`: attach to entity type, handle entity lifecycle events
- [ ] Implement `LevelBlueprint`: attach to world/dimension, handle world events
- [ ] Implement `FunctionBlueprint`: pure function with inputs/outputs, no event nodes
- [ ] Create `BlueprintInterface`: define function signatures that blueprints must implement
- [ ] Implement interface implementation checking (validate blueprint has required functions)
- [ ] Create `MacroLibrary`: save node groups as reusable macros
- [ ] Add macro collapse/expand (turn selection into macro node)
- [ ] Implement function library (static utility functions, like UE4's BlueprintFunctionLibrary)
- [ ] Add blueprint inheritance (child blueprints extend parent blueprints)

---

### Phase 9: Performance & Optimization

**Objectives:**
- Optimize execution engine for production use
- Add performance monitoring and limits
- Implement debugging tools

**Deliverables:**
- Execution time limits (prevent server lag)
- Node execution budgets (max nodes per tick)
- Blueprint debugger (step through execution)
- Performance profiling tools

**Key Classes:**

```java
// execution/PerformanceMonitor.java
public class PerformanceMonitor {
    private static final long MAX_EXECUTION_TIME_MS = 50;
    private static final int MAX_NODES_PER_EXECUTION = 10000;

    private long executionStartTime;
    private int nodesExecuted;

    public void checkLimits() throws ExecutionTimeoutException {
        if (System.currentTimeMillis() - executionStartTime > MAX_EXECUTION_TIME_MS) {
            throw new ExecutionTimeoutException("Blueprint execution exceeded 50ms");
        }
        if (nodesExecuted > MAX_NODES_PER_EXECUTION) {
            throw new ExecutionLimitException("Blueprint executed too many nodes");
        }
    }
}
```

```java
// debug/BlueprintDebugger.java
public class BlueprintDebugger {
    private final Map<IBlueprintNode, Integer> nodeExecutionCounts = new HashMap<>();
    private final List<ExecutionStep> executionHistory = new ArrayList<>();

    public void recordNodeExecution(IBlueprintNode node, ExecutionContext context) {
        nodeExecutionCounts.merge(node, 1, Integer::sum);
        executionHistory.add(new ExecutionStep(node, context.snapshot()));
    }
}
```

**Checklist:**
- [ ] Add execution time tracking (start time, elapsed time)
- [ ] Implement max execution time limit (default 50ms, configurable)
- [ ] Implement max nodes per execution limit (prevent infinite loops)
- [ ] Add performance profiling (track time spent in each node)
- [ ] Create performance report (which nodes are slowest)
- [ ] Implement breakpoints (pause execution at specific nodes)
- [ ] Add step-by-step execution mode (step over, step into)
- [ ] Create execution history viewer (see order of node execution)
- [ ] Add variable inspector (view all variables in context)
- [ ] Implement hot reload (recompile blueprint without restarting server)
- [ ] Add execution analytics (how often each blueprint runs)
- [ ] Optimize canvas rendering (only render visible nodes, use batching)

---

### Phase 10: Polish & User Experience

**Objectives:**
- Create comprehensive in-game tutorials
- Build blueprint repository/dashboard
- Add blueprint sharing and import/export

**Deliverables:**
- Tutorial blueprints (guided learning)
- Blueprint dashboard GUI (browse, search, filter)
- Blueprint sharing system (export/import)
- Documentation and examples

**Key Features:**

```java
// ui/BlueprintDashboard.java
public class BlueprintDashboard extends Screen {
    private final List<BlueprintMetadata> allBlueprints;
    private final List<BlueprintMetadata> runningBlueprints;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderTabs(context); // All | Favorites | Running
        renderBlueprintGrid(context);
        renderActionButtons(context); // New, Import, Export
    }
}
```

**Checklist:**
- [ ] Create blueprint dashboard screen (access via command or hotkey)
- [ ] Implement blueprint list with thumbnails (render graph preview)
- [ ] Add filters: type (Entity/Level/Function), status (running/stopped), favorites
- [ ] Add search functionality (search by name, description, tags)
- [ ] Implement blueprint execution from dashboard (run, stop, restart)
- [ ] Create tutorial system (step-by-step guided blueprint creation)
- [ ] Add example blueprints (mob spawner, redstone alternative, mini-games)
- [ ] Implement export to file (JSON or custom format)
- [ ] Implement import from file (drag-drop or file picker)
- [ ] Add blueprint sharing (upload to server, download from community)
- [ ] Create in-game help system (tooltips, node descriptions)
- [ ] Add blueprint versioning (track changes, rollback)
- [ ] Implement blueprint permissions (who can edit/execute)
- [ ] Add blueprint analytics dashboard (execution stats, errors)

---

## 🧩 Core Classes

### BlueprintGraph

**Purpose**: Container for the entire node graph

```java
public class BlueprintGraph {
    private final String id;
    private final String name;
    private final List<IBlueprintNode> nodes = new ArrayList<>();
    private final List<IBlueprintConnection> connections = new ArrayList<>();

    public void addNode(IBlueprintNode node) {
        nodes.add(node);
    }

    public void addConnection(IBlueprintPin from, IBlueprintPin to) {
        if (from.canConnectTo(to)) {
            connections.add(new BlueprintConnection(from, to));
            from.connectTo(to);
        }
    }

    public List<IBlueprintNode> getEventNodes() {
        return nodes.stream()
            .filter(node -> node instanceof EventNode)
            .collect(Collectors.toList());
    }
}
```

### BlueprintPin

**Purpose**: Connection point on nodes with type safety

```java
public class BlueprintPin implements IBlueprintPin {
    private final String id;
    private final String name;
    private final PinType type;
    private final PinDirection direction;
    private final IBlueprintNode owner;
    private final Set<IBlueprintPin> connections = new HashSet<>();

    @Override
    public boolean canConnectTo(IBlueprintPin other) {
        // Execution pins can only connect to execution pins
        if (type == PinType.EXECUTION_FLOW) {
            return other.getType() == PinType.EXECUTION_FLOW
                && direction != other.getDirection();
        }

        // Data pins must match type
        return type == other.getType()
            && direction != other.getDirection();
    }

    @Override
    public void connectTo(IBlueprintPin other) {
        if (!canConnectTo(other)) {
            throw new InvalidConnectionException("Cannot connect " + type + " to " + other.getType());
        }
        connections.add(other);
        other.connections.add(this);
    }
}
```

### ExecutionContext

**Purpose**: Runtime state during blueprint execution

```java
public class ExecutionContext {
    private final Map<String, Object> variables = new ConcurrentHashMap<>();
    private final World world;
    private final Entity entity; // Can be null for level blueprints
    private final Stack<IBlueprintNode> callStack = new Stack<>();

    public <T> T getVariable(String name, Class<T> type) {
        Object value = variables.get(name);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new TypeMismatchException("Variable " + name + " is not of type " + type);
        }
        return type.cast(value);
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public <T> T getPinValue(String pinId, Class<T> type) {
        // Trace back through connections to find value
        // For pure nodes, execute them on-demand
    }

    public void executeConnectedNodes(IBlueprintNode source, String outputPinId) {
        IBlueprintPin pin = source.getOutput(outputPinId);
        if (pin == null) return;

        for (IBlueprintPin connectedPin : pin.getConnections()) {
            IBlueprintNode nextNode = connectedPin.getNode();
            callStack.push(nextNode);
            nextNode.execute(this);
            callStack.pop();
        }
    }
}
```

### NodeRegistry

**Purpose**: Factory for creating nodes by ID

```java
public class NodeRegistry {
    private static final Map<String, Supplier<IBlueprintNode>> REGISTRY = new HashMap<>();

    static {
        register("on_block_use", OnBlockUseEventNode::new);
        register("branch", BranchNode::new);
        register("set_block", SetBlockNode::new);
        register("add", AddNode::new);
        // ... register all node types
    }

    public static void register(String id, Supplier<IBlueprintNode> factory) {
        REGISTRY.put(id, factory);
    }

    public static IBlueprintNode create(String id) {
        Supplier<IBlueprintNode> factory = REGISTRY.get(id);
        if (factory == null) {
            throw new UnknownNodeTypeException("Unknown node type: " + id);
        }
        return factory.get();
    }

    public static List<NodeDefinition> getAllNodeDefinitions() {
        // Return metadata for all registered nodes (for palette)
    }
}
```

---

## ⚡ Event System

### Fabric Event Integration

The mod intercepts Fabric events and translates them into blueprint event nodes.

**Event Registration Pattern:**

```java
// In MinecraftBlueprints.onInitialize()
UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
    if (world.isClient()) return ActionResult.PASS;

    // Find all blueprints listening to this event
    List<Blueprint> blueprints = BlueprintRegistry.getBlueprintsForEvent("on_block_use");

    for (Blueprint blueprint : blueprints) {
        ExecutionContext context = new ExecutionContext(world, player);
        context.setVariable("player", player);
        context.setVariable("blockPos", hitResult.getBlockPos());
        context.setVariable("world", world);
        context.setVariable("hand", hand);

        blueprint.executeEvent("on_block_use", context);
    }

    return ActionResult.PASS;
});
```

### Event Node Mappings

| Blueprint Event | Fabric Event | Output Pins |
|----------------|--------------|-------------|
| **On Block Use** | `UseBlockCallback.EVENT` | player, blockPos, world, hand, side |
| **On Block Break** | `PlayerBlockBreakEvents.BEFORE` | player, blockPos, world, blockState |
| **On Entity Death** | `ServerLivingEntityEvents.AFTER_DEATH` | entity, world, damageSource |
| **On Server Tick** | `ServerTickEvents.END_SERVER_TICK` | server |
| **On Player Join** | `ServerPlayConnectionEvents.JOIN` | player, server |
| **On Player Leave** | `ServerPlayConnectionEvents.DISCONNECT` | player, server |
| **On Attack Entity** | `AttackEntityCallback.EVENT` | player, world, hand, entity, hitResult |
| **On Use Item** | `UseItemCallback.EVENT` | player, world, hand |
| **On Craft Item** | `CraftItemCallback.EVENT` (custom) | player, itemStack, craftingInventory |

### Custom Event System

For events not provided by Fabric, create custom event hooks:

```java
public class CustomEvents {
    public static final Event<CraftItemCallback> CRAFT_ITEM =
        EventFactory.createArrayBacked(CraftItemCallback.class,
            callbacks -> (player, itemStack, inventory) -> {
                for (CraftItemCallback callback : callbacks) {
                    ActionResult result = callback.onCraftItem(player, itemStack, inventory);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            }
        );

    @FunctionalInterface
    public interface CraftItemCallback {
        ActionResult onCraftItem(PlayerEntity player, ItemStack result, CraftingInventory inventory);
    }
}
```

---

## 📦 Blueprint Nodes

### Event Nodes

```
┌─────────────────────────┐
│   On Block Use Event    │
├─────────────────────────┤
│ ▷ Exec ─────────────────┤ Execution output
│ ● Player ───────────────┤ ServerPlayerEntity
│ ● Block Pos ────────────┤ BlockPos
│ ● World ────────────────┤ World
│ ● Hand ─────────────────┤ Hand (MAIN/OFF)
└─────────────────────────┘
```

### Flow Control Nodes

**Branch (If-Else)**
```
┌─────────────────────────┐
│        Branch           │
├─────────────────────────┤
├───────────── Exec ●     │ Execution input
├───────────── Condition ●│ Boolean input
│ ▷ True ─────────────────┤ Exec if true
│ ▷ False ────────────────┤ Exec if false
└─────────────────────────┘
```

**Sequence**
```
┌─────────────────────────┐
│       Sequence          │
├─────────────────────────┤
├───────────── Exec ●     │ Execution input
│ ▷ Then 0 ───────────────┤ First output
│ ▷ Then 1 ───────────────┤ Second output
│ ▷ Then 2 ───────────────┤ Third output
└─────────────────────────┘
```

**For Loop**
```
┌─────────────────────────┐
│       For Loop          │
├─────────────────────────┤
├───────────── Exec ●     │
├───────────── First Index ●│ Int (start)
├───────────── Last Index ●│ Int (end)
│ ▷ Loop Body ────────────┤ Exec each iteration
│ ● Index ────────────────┤ Current index
│ ▷ Completed ────────────┤ Exec after loop
└─────────────────────────┘
```

### Action Nodes

**Set Block**
```
┌─────────────────────────┐
│       Set Block         │
├─────────────────────────┤
├───────────── Exec ●     │
├───────────── World ●    │
├───────────── Pos ●      │
├───────────── Block State ●│
│ ▷ Then ─────────────────┤
└─────────────────────────┘
```

**Spawn Entity**
```
┌─────────────────────────┐
│      Spawn Entity       │
├─────────────────────────┤
├───────────── Exec ●     │
├───────────── World ●    │
├───────────── Pos ●      │
├───────────── Entity Type ●│
│ ▷ Then ─────────────────┤
│ ● Spawned Entity ───────┤
└─────────────────────────┘
```

**Send Message**
```
┌─────────────────────────┐
│     Send Message        │
├─────────────────────────┤
├───────────── Exec ●     │
├───────────── Player ●   │
├───────────── Message ●  │ String
│ ▷ Then ─────────────────┤
└─────────────────────────┘
```

### Variable Nodes

**Get Variable**
```
┌─────────────────────────┐
│     Get Variable        │
├─────────────────────────┤
├───────────── Name ●     │ String (variable name)
│ ● Value ────────────────┤ Any type
└─────────────────────────┘
```

**Set Variable**
```
┌─────────────────────────┐
│     Set Variable        │
├─────────────────────────┤
├───────────── Exec ●     │
├───────────── Name ●     │
├───────────── Value ●    │
│ ▷ Then ─────────────────┤
└─────────────────────────┘
```

### Math Nodes (Pure)

**Add**
```
┌─────────────────────────┐
│          Add            │
├─────────────────────────┤
├───────────── A ●        │ Float/Int
├───────────── B ●        │ Float/Int
│ ● Result ───────────────┤ Float/Int
└─────────────────────────┘
```

---

## 🔧 Technical Integration

### Client-Server Networking

**Packet Registration** (`MinecraftBlueprints.java`):
```java
@Override
public void onInitialize() {
    // Register S2C packets
    PayloadTypeRegistry.playS2C().register(
        BlueprintDataPayload.ID, BlueprintDataPayload.CODEC
    );

    // Register C2S packets
    PayloadTypeRegistry.playC2S().register(
        SaveBlueprintPayload.ID, SaveBlueprintPayload.CODEC
    );
}
```

**Packet Handling** (`MinecraftBlueprintsClient.java`):
```java
@Override
public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(BlueprintDataPayload.ID,
        (payload, context) -> {
            context.client().execute(() -> {
                BlueprintGraph graph = BlueprintSerializer.deserialize(payload.graphJson());
                BlueprintEditor.loadGraph(graph);
            });
        }
    );
}
```

**Sending Packets**:
```java
// Client → Server: Save blueprint
ServerPlayNetworking.send(new SaveBlueprintPayload(blueprintId, graphJson));

// Server → Client: Send blueprint data
ServerPlayNetworking.send(player, new BlueprintDataPayload(blueprintId, graphJson));
```

### Performance Safeguards

**Execution Limits**:
- Max 50ms execution time per blueprint
- Max 10,000 nodes executed per invocation
- Max 100 blueprints executing per tick
- Cycle detection in graph validation

**Resource Pooling**:
- Reuse `ExecutionContext` objects
- Connection pooling for database access
- Canvas rendering optimization (cull off-screen nodes)

---

## 📚 Development Guide

### Creating a Custom Node

1. **Extend BlueprintNode**:
```java
public class MyCustomNode extends BlueprintNode {
    public MyCustomNode() {
        super("my_custom_node", "My Custom Node");
        addInput(PinType.EXECUTION_FLOW, "exec");
        addInput(PinType.STRING, "message");
        addOutput(PinType.EXECUTION_FLOW, "then");
    }

    @Override
    public void execute(ExecutionContext context) {
        String message = context.getPinValue("message", String.class);
        // Your custom logic here
        MinecraftBlueprints.LOGGER.info("Custom node: " + message);
        context.executeConnectedNodes(this, "then");
    }

    @Override
    public void validate() {
        // Validate that required inputs are connected
        if (getInput("message") == null || !getInput("message").isConnected()) {
            throw new ValidationException("Message input must be connected");
        }
    }
}
```

2. **Register in NodeRegistry**:
```java
NodeRegistry.register("my_custom_node", MyCustomNode::new);
```

3. **Add to Node Palette**:
```java
categories.get("Custom").add(new NodeDefinition(
    "my_custom_node",
    "My Custom Node",
    "Does something custom",
    "Custom"
));
```

### Extending with Third-Party Mods

The API module allows other mods to create custom nodes:

```java
// In your mod's initializer
public void onInitialize() {
    NodeRegistry.register("my_mod_node", MyModNode::new);

    // Add custom pin types
    PinTypeRegistry.register(new PinType("MyCustomType", MyClass.class, 0xFF123456));
}
```

### Best Practices

1. **Always validate connections**: Use `pin.canConnectTo()` before creating connections
2. **Limit execution time**: Use `PerformanceMonitor` to prevent lag
3. **Test with large graphs**: Ensure your nodes perform well with 100+ node graphs
4. **Handle nulls gracefully**: Check for null pin values
5. **Use thread-safe collections**: Blueprints may execute on different threads
6. **Log errors properly**: Use `MinecraftBlueprints.LOGGER` for debugging
7. **Document your nodes**: Add tooltips and descriptions for users

---

## 🚀 Getting Started

### Building the Project

```bash
# Clone the repository
git clone https://github.com/your-username/minecraft-blueprints.git
cd minecraft-blueprints

# Build both modules
./gradlew build

# Run client with mod
./gradlew runClient

# Run server with mod
./gradlew runServer
```

### Testing the Blueprint System

1. Launch Minecraft with the mod installed
2. Craft a Blueprint Workbench (recipe TBD)
3. Right-click the workbench to open the editor
4. Drag an **On Block Use Event** node from the palette
5. Connect it to a **Print** node
6. Save the blueprint
7. Right-click any block to trigger your blueprint!

---

## 📄 License

This project is licensed under the CC0-1.0 License.

---

## 🤝 Contributing

Contributions are welcome! Please read the development guide above before submitting PRs.

**Priority Areas**:
- Node implementations (see Phase 7)
- GUI improvements (canvas performance, UX)
- Blueprint examples and tutorials
- Documentation and guides

---

**Made with ❤️ by boul2gom** | Inspired by Unreal Engine Blueprints
