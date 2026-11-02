# Architecture

## Target

Minecraft Java 26.3, Paper server, Java 25. This is a server-side Paper plugin. The current interaction code requires neither a client gameplay mod nor a resource pack.

## Vanilla pose strategy

The former ItemDisplay/resource-pack animation path is no longer called. The original Mob remains visible and at its native size. The plugin freezes it, uses Mob.lookAt(location, headRotationSpeed, maxHeadPitch) to smoothly direct the vanilla head/look controller toward a point almost directly above the Mob. LivingEntity.setBodyYaw holds the body orientation. The entity's own pitch and dimensions are not changed by the chest animation.

This is an approximation, not a true chest hinge or independent model-part animation. It uses the vanilla look controller where available. Paper's public Bukkit API does not expose independent tentacle, abdomen, or arbitrary model-bone controls.

## State machine

NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED

First click freezes and applies a mild tremble. Second click smoothly raises the vanilla head/look controller and opens one GUI session. Closing the GUI returns the original pose and AI/physics. After use, the mob cannot open another chest.

## Chest inventory persistence and death drops

The plugin serializes the 54-slot GUI with Paper's ItemStack.serializeItemsAsBytes into the mob's PersistentDataContainer. Inventory click, drag, close and player disconnect paths update the stored snapshot.

When a used mob dies, the plugin clears vanilla item drops and death XP, then drops only the remaining serialized chest items. If the chest was never opened, vanilla drops are untouched. Loot ejected during opening is removed from the GUI before spawning, preventing duplication.

## Loot dimensions

Loot selection receives the mob's current world environment. Overworld pools favor obsidian, flint and steel, and navigation/crafting supplies. Nether pools add quartz, blaze rods, gold and rare Netherite progression items. End pools include Ender and Shulker progression materials.

## Limitations

- No custom resource pack is required, so there are no custom mob or item textures in this mode.
- Squid tentacles and other independent model parts cannot be animated precisely by the generic head-look method.
- Liquid Gold fluid flow/infinite source and its ten-second powers are not implemented.
- Real Paper 26.3 in-game and multiplayer QA remains a release gate.
