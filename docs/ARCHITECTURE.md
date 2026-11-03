# Architecture

## Target

Minecraft Java 26.3, Paper server, Java 25. The current plugin requires neither a client gameplay mod nor a resource pack.

## Stable vanilla pose

The original Mob remains visible at its native dimensions. On the first click the plugin saves the original AI/awareness/gravity/collision state and its exact position/rotation, then disables AI and awareness.

The opening animation applies `Entity.setRotation(yaw, pitch)` directly, with body yaw restored to the saved orientation. The pitch moves from the Mob's saved pitch to approximately -88 degrees. Since AI is disabled throughout OPENING, LOOT_OPEN and CLOSING, normal look goals cannot repeatedly overwrite the pose. While the GUI is open, a hold task pins the Mob and keeps that pitch fixed.

This is a pitch-based approximation, not independent head-bone manipulation or a real hinged chest model. Mob renderers may interpret pitch differently. The dimensions and hitbox are not changed.

## State machine

NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED

First click freezes and trembles subtly. Second click animates the pitch and opens one GUI. Closing consumes the chest and restores the original Mob state. A used Mob cannot open another chest.

## Enchantment books

Custom books store an enchantment key and bounded level in PersistentDataContainer, not unsafe stored-enchantment data. This avoids client tooltip translation keys for very high vanilla stored levels.

PlayerInteractEvent is handled for both hands, including initially-cancelled right-click-air events. While a custom book is in the main hand, the off-hand event is cancelled so armour/tools are not equipped or used before the plugin reads the target. The plugin validates `Enchantment.canEnchantItem`, existing levels and conflicts, then applies the bounded level with `addUnsafeEnchantment`. The book is consumed only after success.

Ordinary Vanilla Enchanted Books are generated separately with standard stored levels and remain usable in an anvil.

## Loot and dimension rules

The generator creates 14–18 base occupied stacks plus separate special reward rolls. It mixes family pools with blocks, stone variants, dyes, bones, ink, books, potions, lanterns, trims, food, crafting resources and decor. Maps are restricted to explorer/trader-like Mob types.

The current world environment controls progression:
- Overworld: obsidian, flint and steel, iron and common building supplies.
- Nether: quartz, blaze rods, gold and rare ancient debris/Netherite progression.
- End: Ender, chorus, End Stone and Shulker progression.

A 50% burst (rounded up) is removed from the GUI before being spawned as real item entities. Reduced horizontal velocity targets a roughly 3–4 block scatter radius. The remaining GUI contents are serialized to the Mob's PersistentDataContainer.

When a used Mob dies, vanilla item drops are cleared and the remaining saved chest items are dropped naturally. Normal vanilla XP remains. A Mob whose chest was never opened keeps normal vanilla item drops and XP.

## Limitations

- No custom resource pack is required; custom item textures are therefore unavailable.
- The generic pitch pose is not a true lid hinge. Independent squid tentacles and arbitrary model bones need a custom renderer.
- Liquid Gold fluid flow/infinite sources and its ten-second powers are not implemented.
- Individual artifact abilities, Legend points and boss progression remain future work.
- CI validates compilation/package contents, not visual appearance in a live Paper client.
