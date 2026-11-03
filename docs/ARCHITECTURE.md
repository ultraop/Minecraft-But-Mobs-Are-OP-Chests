# Architecture

## Target and components

Minecraft Java 26.3, Paper server, Java 25. The server uses a Paper plugin. A companion resource pack is optional for gameplay but required to display Liquid Gold's distinct golden bucket and flow textures. No client gameplay mod, Fabric API or datapack is required.

## Liquid Gold service

LiquidGoldService uses vanilla WATER block states as the physical substrate for swimming and collision, but cancels normal vanilla fluid spread for the tracked cells. A scheduled server task performs a bounded breadth-first flow simulation, with horizontal and vertical limits from config.

- Source and flow cells are tracked by world/coordinates.
- Per-cell ItemDisplay entities provide the pale gold visual overlay; display PDC stores level, source flag and coordinates so cells can be rediscovered on chunk load.
- Two diagonally opposite source cells in a supported 2×2 square create the other two source cells.
- Right-click a block with Liquid Gold to pour into the adjacent block, subject to BlockPlaceEvent protection checks.
- Right-click a source with an empty bucket to collect it.
- Right-click air with the filled bucket to drink it and return an empty bucket.
- Player bounding-box intersection with tracked cells grants the same 10-second powers; the timer refreshes while the player stays immersed.
- Normal Water/Lava and Liquid Gold flow are isolated from one another.

The plugin simulation manages flow, source rules, bucket operations and effects. The resource pack only draws the golden colour. This is water-backed plugin simulation rather than a new native fluid block registered inside the Minecraft engine.

## Mob chest lifecycle

The original Mob remains visible and at native dimensions. AI is disabled during opening and the vanilla entity pitch is animated while body yaw remains at its saved orientation. This is not a true split-body hinge; separate tentacle/bone animations require a custom renderer.

State: NORMAL → FROZEN → OPENING → LOOT_OPEN → CLOSING → LOOTED.

Loot stacks are generated once, saved on the Mob and refreshed as the GUI changes. Half the stacks are ejected before the GUI opens; the other half remains in the GUI. Used Mob death drops only the remaining chest loot while preserving normal XP.

## Runtime safety

- Flow task is cancelled on plugin disable; cell data remains in persistent chunk ItemDisplay metadata.
- Chunk load re-discovers fluid cells and schedules flow recalculation.
- Flowing cells cannot be bucket-collected; only source blocks return a Liquid Gold Bucket.
- Region protection is checked for placement using a BlockPlaceEvent.
- Liquid Gold uses separate item models/textures from ordinary Water/Lava.
