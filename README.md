# UltraOP — Mobs Are OP Chests

**Paper 26.3 plugin | Java 25 | No resource pack or client mod required**

![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Visuals](https://img.shields.io/badge/Visuals-Vanilla%20entity%20pose-blue)

## Current approach

The old ItemDisplay/resource-pack animation path is no longer used by the interaction code. This build keeps the real vanilla mob visible at its normal size and uses Paper's Mob.lookAt head controller to raise the vanilla head/front while holding body yaw steady. It does not rotate or scale a replacement model.

- No .minecraft/resourcepacks installation is required.
- No Fabric, Forge, NeoForge, Fabric API or client mod is required.
- Only the Paper server plugin JAR is required.
- No mob scale attribute or model replacement is applied.
- Mob-specific vanilla renderers may interpret head-look instructions differently. A head/front lift is not guaranteed to animate squid tentacles or every complex model part independently.

## Gameplay

1. First right-click freezes a Mob at its saved position. It gives a very subtle head-look tremble, not a sit/tame animation.
2. Second right-click smoothly instructs the Mob head to look almost straight upward (88 degrees by default), while its body yaw is held in place.
3. A chest/firework sound and a small particle burst play.
4. 50% of the generated item stacks pop outward in a stronger radial burst. Those stacks are removed from the GUI before spawning, preventing duplicate collection.
5. The remaining items are shown in a one-time inventory. Closing after taking all, some or none consumes the chest.
6. The vanilla pose returns to its original rotation and the mob's saved AI/awareness/gravity/collision settings are restored.
7. The mob can then behave and be killed normally. If the chest was used, its remaining saved chest contents replace vanilla mob drops and XP. If its chest was never opened, normal vanilla drops remain.

## Mob coverage

By default, all Bukkit Mob entity types are enabled. Set prototype.support-all-mobs: false and list prototype.supported-entities in plugins/UltraOPMobChests/config.yml to restrict them. Add entity names to prototype.excluded-entities to exclude special cases.

The visual method is intentionally generic and resource-pack-free. It uses Paper's Mob.lookAt head-rotation control; it does not replace the Mob model or change its dimensions. Most mobs with a vanilla head-look controller can raise their head/front. Squid tentacles and other independent model parts cannot be animated by this API and need a custom renderer for exact part-by-part animation.

## Loot

- Variable 14–18 base item stacks per chest (plus special rewards), with larger stack quantities.
- Mob-family drops and broad utility loot such as paper, compass, map, clock, book, arrows, metals and crafting materials.
- Dimension-aware progression rewards:
  - Overworld: obsidian, flint and steel, iron and navigation supplies.
  - Nether: quartz, blaze rods, gold, ancient debris and Netherite scrap/ingot in rare pools.
  - End: Ender-related materials, shulker shells, chorus and Dragon-related rare items.
- Rare Golden Potato, Golden Porkchop and Golden Bread names/IDs.
- Green-dyed Emerald Armor pieces, one equipment piece maximum per chest.
- Liquid Gold Bucket reward with a 35% test-build drop chance; its icon uses a vanilla orange-filled bucket.
- Custom enchantment tome rank metadata from 30–60, separate from vanilla levels.

Without a resource pack, golden foods use vanilla item artwork with their custom names/lore; custom item textures are not supplied by this build.

## Chest loot on mob death

The live GUI contents are serialized onto the mob using Paper's ItemStack byte serialization. Inventory click/drag and close events refresh that saved snapshot. After a chest has been used:
- vanilla mob item drops are cleared;
- vanilla XP is preserved;
- only the remaining serialized chest contents are dropped naturally.

Loot stacks ejected at opening are removed from the GUI first, so they are not included in the saved remainder. A chest that was never opened keeps normal vanilla death drops.

## Known limitations

- This is a head/front look animation, not an actual model split with a hinged 3D lid. Paper's public entity API does not expose independent rotation controls for every model part. Squid tentacles and other complex parts need a client renderer or model/animation system to animate separately.
- The resource-pack-free version cannot provide custom item textures. Golden food is identified through names and metadata; Emerald Armor is green-dyed leather armor.
- Liquid Gold fluid placement, flowing water-like behavior, two-source infinite source and the requested 10-second powers remain unimplemented. The current bucket is a named placeholder that is prevented from placing ordinary water.
- Named artifact abilities, custom enchantment effects, Legend points and boss progression remain future work.
- The code has passed CI compilation and static package checks, but live Paper 26.3 client tests are still required for actual head-look appearance, clipping, every mob renderer, inventory persistence, death drops and multiplayer behavior.

## Requirements and install

| Requirement | Target |
|---|---|
| Minecraft Java | 26.3 |
| Server | Paper 26.3 |
| Java | 25 |
| Client gameplay mod | Not required |
| Resource pack | Not required |
| Datapack | Not required |

1. Put the JAR in the Paper server's plugins/ folder.
2. Disable/remove the old UltraOP Mob Chests resource pack from the client; this build does not use it.
3. Remove the old UltraOP Mob Chests datapack ZIP from the world's datapacks folder; this build does not use it.
4. Start Paper 26.3 with Java 25.
5. Run /mobchests info and /mobchests status.
6. First right-click a mob to freeze it; second right-click opens the pose and loot GUI.
7. Close the GUI, then kill the mob to verify that only the remaining chest items drop.

## Build

~~~bash
gradle clean build
~~~

Windows:

~~~powershell
gradle.bat clean build
~~~

## Commands

| Command | Permission | Purpose |
|---|---|---|
| /mobchests info | Everyone | Version and implemented-scope information |
| /mobchests help | Everyone | Command guide |
| /mobchests status | Everyone | Runtime and prototype status |
| /mobchests reload | mobchests.admin | Reload configuration |

## License

MIT — see LICENSE.
