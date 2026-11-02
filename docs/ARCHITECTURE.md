# Architecture

## Target

Minecraft Java 26.3, Paper server, Java 25. This is a server-side Paper plugin. The current interaction code requires neither a client gameplay mod nor a resource pack.

## Vanilla pose strategy

The former ItemDisplay/resource-pack animation path is no longer called. The original Mob remains visible and at its native size. The plugin freezes it, uses Entity.setRotation(yaw, pitch) to interpolate the vanilla pitch toward -90 degrees, and restores the original pitch afterwards. LivingEntity.setBodyYaw is used to hold body orientation while the pitch pose changes.

This is an approximation, not a true chest hinge or independent model-part animation. Many vanilla Mob renderers map entity pitch to the head/front pose, but complex entities may interpret it differently. Paper's public Bukkit API does not expose independent tentacle, abdomen, or arbitrary model-bone controls.

## State machine

NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED

First click freezes and applies a mild tremble. Second click interpolates pitch upward and opens one GUI session. Closing the GUI returns the original pose and AI/physics. After use, the mob cannot open another chest.

## Chest inventory persistence and death drops

The plugin serializes the 54-slot GUI with Paper's ItemStack.serializeItemsAsBytes into the mob's PersistentDataContainer. Inventory click, drag, close and player disconnect paths update the stored snapshot.

When a used mob dies, the plugin clears vanilla item drops and death XP, then drops only the remaining serialized chest items. If the chest was never opened, vanilla drops are untouched. Loot ejected during opening is removed from the GUI before spawning, preventing duplication.

## Loot dimensions

Loot selection receives the mob's current world environment. Overworld pools favor obsidian, flint and steel, and navigation/crafting supplies. Nether pools add quartz, blaze rods, gold and rare Netherite progression items. End pools include Ender and Shulker progression materials.

## Limitations

- No custom resource pack is required, so there are no custom mob or item textures in this mode.
- Squid tentacles and other independent model parts cannot be animated precisely by the generic pitch method.
- Liquid Gold fluid flow/infinite source and its ten-second powers are not implemented.
- Real Paper 26.3 in-game and multiplayer QA remains a release gate.
