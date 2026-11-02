# UltraOP — Mobs Are OP Chests

**Paper 26.3 plugin + companion datapack + resource pack**

![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Status](https://img.shields.io/badge/Status-Cow%20Prototype-yellow)

> **Current build:** Cow-only interaction and split-body visual prototype. The full all-mob loot system, custom equipment, Legend progression and boss unlocks are not yet implemented.

## Gameplay goal

Every supported vanilla mob becomes a one-time, living treasure chest:

1. **First right-click:** freeze the mob in place. It does not sit, become a pet or get tamed.
2. **Second right-click:** open the mob's body at its middle seam. The upper part rotates like a chest lid; the lower part stays as the base.
3. **One inventory session:** loot is shown once. Taking all, some or no items and closing the inventory consumes the opening.
4. **Close animation:** close the lid, remove temporary display parts and restore the mob's original AI, awareness, gravity, visibility and collision settings.
5. **Afterwards:** the mob behaves normally again. Killing it later gives its ordinary vanilla drops and XP, not extra chest loot.

## Current proof of concept

The initial visual target is an **adult Cow only**.

- The real Cow remains the server-side entity and hitbox.
- During the open session, the real Cow is hidden and two resource-pack-backed ItemDisplays represent the upper/lower body.
- The upper model interpolates open and closed; the lower model stays fixed.
- The inventory close path restores the Cow regardless of how many items were taken.
- The state is stored in the entity PersistentDataContainer.
- Chunk unload/reload and plugin enable/disable have recovery paths to avoid stuck invisible or frozen Cows.
- Damage is cancelled only while the Cow is opening or its chest inventory is open. Once looted, normal damage/death returns.

**Important:** This is a stylized Cow model proof of concept, not yet a verified copy of the vanilla Cow renderer. The client-side resource pack and in-game visual QA are required before claiming that the animation is glitch-free or extending it to all mobs.

## Components

| Component | Responsibility |
|---|---|
| `plugin/` | Interaction state, animation lifecycle, GUI session, loot test, persistence, commands |
| `datapack/` | Future mob loot tables, tags, recipes, advancements and data-driven defaults |
| `resource-pack/` | Cow split-body models now; future mob/item models, textures, GUI art and sounds |
| `docs/` | Architecture, commands, gameplay, loot, custom items, roadmap and testing |

The plugin owns gameplay state. The datapack provides data. The resource pack provides visuals.

## Target environment

| Requirement | Target |
|---|---|
| Minecraft | 26.3 |
| Server | Paper |
| Java | 25 |
| Build | Gradle 9.5.1 |
| Client gameplay mod | Not required |
| Custom visuals | Server-delivered or manually installed resource pack |

## Build

Requires a Java 25 JDK and Gradle 9.5.1.

```bash
gradle clean build
python3 resource-pack/tools/package_packs.py
```

Windows:

```powershell
gradle.bat clean build
py resource-pack/tools/package_packs.py
```

Outputs:
- Plugin JAR: `plugin/build/libs/`
- Datapack ZIP: `build/distributions/UltraOP-MobChests-Datapack.zip`
- Resource-pack ZIP: `build/distributions/UltraOP-MobChests-ResourcePack.zip`

GitHub Actions builds the plugin and companion ZIPs and uploads them as one workflow artifact.

## Install prototype

1. Build the plugin and place its JAR in Paper's `plugins/` directory.
2. Build the resource pack using the command above.
3. Install or host `UltraOP-MobChests-ResourcePack.zip` and make the client accept it.
4. Start Paper 26.3 using Java 25.
5. Run `/mobchests info`.
6. Test with an adult Cow: first right-click freezes; second right-click starts the open animation and inventory. Close the inventory with all, some or none of the items taken; the Cow should close and resume normal behavior.

The prototype currently supports Cow only. Other mobs are deliberately not intercepted yet.

## Commands

All commands use one root: `/mobchests`.

| Command | Permission | Purpose |
|---|---|---|
| `/mobchests info` | Everyone | Single information/version panel: plugin, Minecraft target, Java, components and feature status |
| `/mobchests help` | Everyone | Command guide |
| `/mobchests status` | Everyone | Runtime and prototype status |
| `/mobchests reload` | `ultraopmobchests.admin` | Reload configuration |
| `/mobchests loot preview <mob>` | Admin | Planned: inspect a mob's loot pools |
| `/mobchests loot test <mob> [rolls]` | Admin | Planned: simulate loot for balance |
| `/mobchests points <player>` | Admin | Planned: inspect Legend points |
| `/mobchests legend [player]` | Everyone | Planned: show Legend progression |
| `/mobchests boss status [player]` | Everyone | Planned: boss unlock status |

`/mobchests info` is the only version/build information command; there is no separate `version` command.

## Next implementation phases

1. Manually verify Cow geometry, seam alignment, open/close animation and cleanup in a real 26.3 client.
2. Test two players, player disconnect, entity death, chunk unload/reload and server restart.
3. Extract the animation pipeline into per-mob model definitions.
4. Implement the full data-driven mob-specific loot engine with varied quantities, rarity and rare custom gear.
5. Add custom item abilities, Legend points and boss progression.

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

MIT — see [LICENSE](LICENSE).
