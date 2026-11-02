# UltraOP Mob Chests Resource Pack

**Target:** Minecraft Java 26.3 — Resource Pack format 97.1.

## Current mob models

### Cow

- cow_base: lower body/base; remains fixed.
- cow_lid: upper body/lid; rotates from the y=8 middle seam.

### Creeper

- creeper_base: lower body and legs; remains fixed.
- creeper_head: head model; rotates backward from the neck seam like a chest lid.

The original living mob remains the server-side entity and hitbox. It is hidden during the animation/GUI session while temporary ItemDisplays show the split model parts. These are stylized models, not replacements for the vanilla client renderer.

## Rare item art

The pack includes custom item icons for Golden Potato, Golden Porkchop, Golden Bread and Liquid Gold Bucket. Emerald Armor currently uses green-dyed leather armor metadata.

## Build and validate

From the repository root:

~~~bash
python resource-pack/tools/package_packs.py
python resource-pack/tools/validate_assets.py
~~~

This generates mob/item PNG textures and creates:
- build/distributions/UltraOP-MobChests-ResourcePack.zip
- build/distributions/UltraOP-MobChests-Datapack.zip

## Visual QA gate

Before calling the visual work complete, test in an actual 26.3 client:

1. Cow model scale/orientation and exact middle seam.
2. Creeper head hinge at the neck seam and stable lower body.
3. No clipping during either lid rotation.
4. No original mob mesh visible while displays are active.
5. No floating display entities after GUI close, death, chunk unload or restart.
6. All players see the same model and animation.
7. Custom item icons render correctly in inventory and hotbar.

Do not claim glitch-free visuals until these client-side tests pass.
