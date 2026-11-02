# Architecture

## Target
Minecraft Java 26.3, Paper server, Java 25. Main implementation: Paper plugin. Companion data: datapack. Client visuals: server resource pack.

The plugin owns all authoritative gameplay state. The datapack provides data-driven loot and progression defaults. The resource pack supplies models, textures, icons and sounds.

## Modules
- interaction: first/second right-click state machine
- entity: persistent state and AI handling
- animation: display/model-part animation
- loot: weighted mob-specific tables and one-time result persistence
- inventory: chest GUI and session safety
- items: custom items, lore and abilities
- enchantments: custom ranks and effects
- progression: Legend points and discoveries
- boss: unlocks, spawn eggs and boss reward flow
- storage: durable entity/player data
- command: /mobchests command tree

## Visual strategy and limitation
Paper does not replace the vanilla client renderer. The initial approach uses resource-pack-backed models and server-controlled display/model parts with synchronized transformations. Prototype one cow first: upper half as hinged lid, lower half as fixed base, with no clipping or duplicate body.

If the desired split-body effect cannot be achieved cleanly, document that limitation and switch to a Fabric client mod for full renderer/model replacement. Do not claim that a display-entity illusion is a native mob-model replacement.

## State machine
NORMAL -> FROZEN -> OPENING -> LOOT_SESSION -> LOOTED -> VANILLA_DEATH

First click freezes without taming/sitting. Second click opens. Generate and persist loot once before GUI display. Reject simultaneous sessions. GUI close consumes the only opening and discards uncollected items. Restore normal AI afterwards. Preserve vanilla drops and XP when the mob later dies. Never grant chest loot on death.

## Persistence
Use a storage abstraction (SQLite suggested) with schema versioning. Entity state and player progression survive chunk unload/reload and restarts. Never identify a mob by entity type alone.

## Data ownership
Plugin: gameplay state and loot authority. Datapack: reloadable tables/default data. Resource pack: visual-only assets.
