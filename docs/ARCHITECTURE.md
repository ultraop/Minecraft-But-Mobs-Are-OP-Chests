# Architecture

## Target
Minecraft Java 26.3, Paper server, Java 25. The main implementation is a Paper plugin. The companion datapack and resource pack provide game data and visuals.

The plugin owns authoritative gameplay state. The datapack is intended for data-driven loot/progression defaults. The resource pack supplies model and texture assets. The current datapack is still a scaffold; the current Cow model is the only implemented split-body visual.

## Modules
- interaction: first/second right-click state machine
- entity: persistent state and AI handling
- animation: display/model-part animation
- loot: mob-family weighted reward generator
- inventory: chest GUI and session safety
- items/enchantments: item IDs and custom rank metadata; abilities/effects remain future work
- progression/boss: future Legend and boss systems

## Visual strategy and limitation
Paper does not replace the vanilla client entity renderer. The prototype hides an adult Cow during the open session and uses resource-pack-backed ItemDisplays for its upper and lower parts. The model is stylized, not an exact copy of the vanilla Cow renderer.

The same display technique must not be assumed to fit every mob shape. Each additional model requires separate geometry and client visual QA. If Paper display entities cannot achieve the required split-body quality, a Fabric client renderer may be necessary.

## State machine
NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED

First click freezes without taming/sitting. Second click opens. Only one inventory session is allowed. GUI close consumes the opening even when no items were taken. Restore original AI/physics state afterwards. Preserve ordinary vanilla death drops and XP. Never grant chest loot again on death.

## Persistence and scope
The mob's state is stored in its PersistentDataContainer. The plugin scans loaded chunks in every loaded world and does not depend on a world name, so a one-world Paper setup is supported. The current runtime interaction is still Cow-only. Durable reward-content serialization and full crash-safe delivery are remaining work.
