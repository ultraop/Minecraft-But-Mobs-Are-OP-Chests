# UltraOP — Mobs Are OP Chests

**Paper 26.3 plugin + companion datapack + resource pack**

![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Status](https://img.shields.io/badge/Status-Cow%20Visual%20Prototype-yellow)

> **Current scope:** adult Cows are the only mob with the two-click freeze/open/close visual interaction. A mob-family loot generator is included, but additional mob interactions remain disabled until each split-body model is created and tested.

## Gameplay goal

Each supported vanilla mob becomes a one-time living treasure chest:

1. First right-click freezes it in place. It does not sit, become a pet or get tamed.
2. Second right-click opens the body at its middle seam. The upper part rotates as a lid; the lower part stays as the base.
3. Only one inventory session is allowed. Taking all, some or no items and closing the inventory consumes the chest.
4. The lid closes, temporary display parts are removed and the original AI/physics state is restored.
5. Afterwards the mob behaves normally. Killing it gives ordinary vanilla drops and XP, not a second chest reward.

## Implemented in this build

- Adult Cow two-click freeze/open lifecycle.
- Persistent one-time entity state and freeze anchor.
- Resource-pack-backed Cow upper/lower ItemDisplay prototype.
- Mob-family common loot profiles for multiple vanilla mob families.
- Variable occupied slots and stack amounts, configurable weighted rarity and vanilla enchanted gear.
- Custom rank 30–60 tome metadata and named custom artifact/equipment IDs.
- One custom equipment piece maximum per chest; 10% default chance, replacing a normal loot slot.
- A small boss rarity bias without guaranteed named rewards.
- Configurable opening and closing animation duration.
- Chunk/plugin recovery paths and ordinary Cow death drops/XP preservation.
- No hard-coded world name or Multiverse dependency.

## Important limitations

- Only adult Cows currently use the chest interaction and split-body animation. Other mob loot families are prepared in code, but other mobs cannot yet be opened as chests.
- Custom item names/IDs and custom enchantment ranks are metadata only. Their abilities and custom enchantment effects are not implemented yet.
- Legend points, boss unlock progression and final data-pack-driven loot tables are not implemented.
- The Cow model is stylized and does not replace the vanilla entity renderer. A real Minecraft 26.3 client with the resource pack must verify seam alignment, rotation and clipping. A successful CI build does not prove visual correctness.

## Single-world / solo use

This is a **Paper server plugin**, not a vanilla single-player mod. It works on a local Paper 26.3 server with one world and one player. It does not require multiple worlds, BungeeCord, Velocity or Multiverse. Put the plugin JAR in the local Paper server's plugins folder and install the resource pack on the Minecraft client.

A normal vanilla single-player world does not load Paper plugins directly. To use this in a solo world, run a local Paper server and join it from your Minecraft client.

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
~~~

Windows:

~~~powershell
gradle.bat clean build
py resource-pack/tools/package_packs.py
~~~

## Install and test the Cow prototype

1. Put the plugin JAR in the Paper server plugins folder.
2. Install or host the resource-pack ZIP for the Minecraft client.
3. Start Paper 26.3 with Java 25.
4. Run /mobchests info.
5. Right-click an adult Cow once to freeze it; right-click it again to open the animation and GUI.
6. Close the GUI after taking all, some or none of the items. The chest is permanently consumed and the Cow should return to normal.

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
