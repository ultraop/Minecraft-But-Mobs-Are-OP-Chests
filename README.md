# UltraOP — Mobs Are OP Chests

**Paper 26.3 plugin + companion datapack + resource pack**

![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Status](https://img.shields.io/badge/Status-Cow%20%2B%20Creeper%20Prototype-yellow)

> **Current visual-interaction scope:** adult Cows and Creepers. The Cow opens at the middle body seam; the Creeper opens its head as a hinged lid. Other mobs remain disabled until their own model/animation is ready.

## Gameplay implemented in this branch

1. First right-click freezes an adult Cow or Creeper in place. It does not sit, become a pet or get tamed.
2. While frozen, the mob has a very subtle alternating 0.65-degree tremble by default. It is configurable and can be disabled.
3. Second right-click starts its model-specific animation:
   - Cow: upper body rotates from the y=8 middle seam; lower body stays as the base.
   - Creeper: head rotates backward from the neck seam; the lower body and legs stay fixed.
4. The opening uses chest/firework sounds and a particle burst. One or two real loot stacks (configurable maximum) are launched upward and outward. Those stacks are removed from the GUI first, so they cannot be duplicated.
5. The remaining items are placed in one inventory session. Closing after taking all, some or no items consumes the chest.
6. The lid closes, temporary display parts are removed and the original mob AI/physics state is restored. A frozen/open Creeper is disarmed; after the chest is consumed, its normal AI and fuse behaviour resume.
7. Killing the mob afterwards gives ordinary vanilla drops and XP, not chest loot a second time.

## Loot in this build

- Mob-family common loot profiles, variable slots and stack sizes.
- Weighted Common-to-Mythic rarity and vanilla enchanted gear.
- Rare Golden Potato, Golden Porkchop and Golden Bread item models.
- Rare Emerald Armor pieces (a single piece per custom-equipment roll, not a full set).
- Rare Liquid Gold Bucket reward metadata with a configurable 7% default chest chance.
- One custom equipment reward at most per chest by default (10% chance).
- Custom tome rank metadata from 30–60, separate from vanilla enchantment levels.

## Important limitations

- Only adult Cows and Creepers currently use the visual chest interaction. Pig, Horse, Zombie, Skeleton, Squid, Sheep, Goat and other mobs remain disabled until their individual model and animation are implemented and tested.
- Golden foods and Liquid Gold bucket use custom resource-pack item art. Emerald Armor is green-dyed leather armor with custom item metadata.
- **Liquid Gold flow, bucket placement/scooping, infinite-source behaviour and its 10-second abilities are not implemented yet.** The current bucket is a safe, non-water custom item placeholder. The exact ability list still needs the effect screenshot.
- Named custom equipment/artifact IDs are loot foundations. Their special powers, custom enchantment effects, Legend points and boss progression are not implemented.
- The models are stylized display-entity illusions, not replacements for Minecraft's vanilla entity renderer. Real Paper 26.3 client testing must verify scale, hinge orientation, clipping, all-viewer synchronization and cleanup. CI cannot certify visual quality.

## Single-world / solo use

This is a Paper server plugin, not a vanilla single-player mod. It works on one Paper 26.3 world with one player and does not require Multiverse, BungeeCord or Velocity. A normal vanilla single-player world does not load Paper plugins directly; use a local Paper server and join it from your Minecraft client.

## Requirements

| Requirement | Target |
|---|---|
| Minecraft Java | 26.3 |
| Server | Paper 26.3 |
| Java | 25 |
| Gradle | 9.5.1 |
| Client gameplay mod | Not required |
| Custom visuals | Resource pack required |

## Build

From the repository root:

~~~bash
gradle clean build
python3 resource-pack/tools/package_packs.py
python3 resource-pack/tools/validate_assets.py
~~~

Windows:

~~~powershell
gradle.bat clean build
py resource-pack/tools/package_packs.py
py resource-pack/tools/validate_assets.py
~~~

## Install and test

1. Put the plugin JAR in the Paper server plugins folder.
2. Install or host the resource-pack ZIP for the Minecraft client.
3. Start Paper 26.3 with Java 25.
4. Run /mobchests info.
5. Test an adult Cow: first right-click freezes with a faint sway; second right-click opens the middle seam.
6. Test a Creeper: first right-click freezes and gently trembles without swelling/igniting; second right-click opens its head.
7. Confirm one or two items burst upward and are collectible; remaining loot appears in the GUI.
8. Close the GUI after taking all, some or no items. The mob returns to normal behaviour and the chest cannot reopen.

## Commands

| Command | Permission | Purpose |
|---|---|---|
| /mobchests info | Everyone | Version and implemented-scope information |
| /mobchests help | Everyone | Command guide |
| /mobchests status | Everyone | Runtime and prototype status |
| /mobchests reload | ultraopmobchests.admin | Reload configuration |

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Commands](docs/COMMANDS.md)
- [Gameplay](docs/GAMEPLAY.md)
- [Loot design](docs/LOOT_DESIGN.md)
- [Custom items](docs/CUSTOM_ITEMS.md)
- [Datapack](docs/DATAPACK.md)
- [Resource pack](docs/RESOURCE_PACK.md)
- [Roadmap](docs/ROADMAP.md)
- [Testing](docs/TESTING.md)

## License

MIT — see LICENSE.
