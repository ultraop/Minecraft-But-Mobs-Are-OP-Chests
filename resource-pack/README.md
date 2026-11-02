# UltraOP Mob Chests Resource Pack

**Target:** Minecraft Java 26.3 — Resource Pack format 97.1.

## Cow chest visual prototype

The current prototype uses two ItemDisplay entities with custom item models:

- `cow_base`: lower body/base; remains fixed.
- `cow_lid`: upper body/lid; rotates open around the middle seam.

The original Cow remains the authoritative living entity and hitbox. It is hidden only during the open/GUI session. When the player closes the GUI—whether they took every item, some items or none—the lid animates shut, both temporary display parts are removed, and the Cow's original visibility, AI, awareness, gravity and collision settings are restored.

The custom resource pack is required. Without it, the ItemDisplay entities use the base item appearance.

## Build

From the repository root:

```bash
python resource-pack/tools/package_packs.py
```

This generates the Cow model PNG textures and creates:
- `build/distributions/UltraOP-MobChests-ResourcePack.zip`
- `build/distributions/UltraOP-MobChests-Datapack.zip`

## Visual QA gate

The model is a stylized Cow proof of concept, not a copy of the exact vanilla Cow renderer. Before expanding to other mobs, test in Minecraft 26.3 for:

1. Scale and orientation from all sides.
2. Exact alignment at the middle seam.
3. Upper lid rotates around its hinge without sliding.
4. Lower body remains still.
5. No original Cow mesh remains visible while open.
6. No floating display parts remain after GUI close.
7. Player takes all, some or no items: every close path restores the Cow.
8. Resource-pack reload, chunk unload/reload and server restart do not leave invisible mobs or orphaned displays.

Do not mark this visual prototype as final or glitch-free until these client-side tests pass.
