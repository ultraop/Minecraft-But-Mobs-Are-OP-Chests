# Gameplay

## Mob chest interaction

1. Mob starts with normal vanilla behaviour.
2. First right-click freezes the Mob in place with a very subtle pitch tremble. It does not sit, become tamed or become a pet.
3. Second right-click applies a smooth vanilla entity-pitch animation toward approximately -88 degrees. The real Mob remains visible with its original dimensions; AI and look goals remain disabled while the chest is open so the pose does not flicker up/down.
4. A chest/firework sound and particles play. Half of the generated loot stacks (rounded up) pop upward with light horizontal scatter, targeting about 3–4 blocks from the Mob.
5. The other half of the loot remains in one inventory GUI. Burst stacks are removed from the GUI before being spawned, so there is no duplicate.
6. Closing the GUI after taking all, some or none of the items consumes the chest permanently. The pose returns to the saved rotation and the Mob's saved AI/physics state is restored.
7. The Mob can then behave and be killed normally. If its chest was used, only the remaining saved chest items replace its normal item drops. Normal XP remains. If the chest was never opened, normal vanilla drops and XP remain.

## Loot persistence

Generate loot once per Mob and save the remaining GUI contents in the Mob's PersistentDataContainer. Inventory clicks, drags and closing refresh the saved contents. Do not reroll by reopening, relogging, chunk unload, ownership change or restart. Prevent simultaneous sessions. Burst items are removed before they are spawned.

## Visual limitations

This is not a real split-body chest hinge. It uses the vanilla entity pitch, with body yaw and native dimensions retained. Different Mob renderers may interpret pitch differently. Squid tentacles and arbitrary model bones cannot be animated independently by the generic Paper API and require a custom client renderer.
