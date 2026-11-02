# UltraOP — Mobs Are OP Chests

**Paper 26.3 plugin + companion datapack + resource pack**

![Minecraft](https://img.shields.io/badge/Minecraft-26.3-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Status](https://img.shields.io/badge/Status-Initial%20Scaffold-yellow)

> **Status:** Initial scaffold only. The buildable plugin currently provides the command, configuration and lifecycle foundation. Mob animation, loot, special items, progression and boss gameplay are planned and are not yet implemented.

## Gameplay goal

Every supported vanilla mob becomes a one-time living treasure chest.

1. First right-click freezes the mob in place (not a pet, sitting or taming action).
2. Second right-click opens the mob's body at its middle seam: upper half opens like a chest lid; lower half stays as the base.
3. One loot GUI session is allowed per mob. Generate and persist the loot once; no reroll or reopening.
4. After the session, restore normal mob AI and appearance.
5. The player can kill the mob normally afterwards; vanilla drops and XP remain. Do not drop special chest loot again.

## Components

| Component | Responsibility |
|---|---|
| `plugin/` | Authoritative state, interactions, inventory GUI, loot, abilities, progression, boss unlocks, persistence, commands |
| `datapack/` | Mob loot tables, tags, recipes, advancements and data-driven defaults |
| `resource-pack/` | Custom split-body models, textures, item visuals, GUI art and sounds |
| `docs/` | Architecture, commands, gameplay rules, loot and rarity, testing, roadmap |

The plugin owns gameplay state. The datapack provides data. The resource pack provides visuals.

## Target environment

| Requirement | Target |
|---|---|
| Minecraft | 26.3 |
| Server | Paper |
| Java | 25 |
| Language/build | Java / Gradle |
| Client mod | Not intended; server resource pack required for custom visuals |

## Build

Requires a Java 25 JDK and Gradle.

```bash
gradle clean build
```

Windows:

```powershell
gradle.bat clean build
```

Plugin JAR: `plugin/build/libs/`.

Zip the companion folders as:
- `UltraOP-MobChests-Datapack.zip`
- `UltraOP-MobChests-ResourcePack.zip`

## Install

1. Put the plugin JAR into Paper's `plugins/` folder.
2. Start the server once and review `plugins/UltraOPMobChests/`.
3. Install the datapack into the target world's `datapacks/` folder.
4. Host the resource pack and configure the server's resource-pack URL and SHA-1.
5. Restart and run `/mobchests info`.

**This initial scaffold is not a completed gameplay release.**

## Commands

All commands use one root: `/mobchests`.

| Command | Permission | Purpose |
|---|---|---|
| `/mobchests info` | Everyone | Single information/version panel: plugin, Minecraft target, Java, components and feature status |
| `/mobchests help` | Everyone | Command guide |
| `/mobchests status` | Everyone | Runtime/component status |
| `/mobchests reload` | `ultraopmobchests.admin` | Reload configuration |
| `/mobchests loot preview <mob>` | Admin | Planned: inspect a mob's loot pools |
| `/mobchests loot test <mob> [rolls]` | Admin | Planned: simulate loot for balance |
| `/mobchests points <player>` | Admin | Planned: inspect Legend points |
| `/mobchests legend [player]` | Everyone | Planned: show Legend progression |
| `/mobchests boss status [player]` | Everyone | Planned: boss unlock status |
| `/mobchests admin reset <player>` | Admin | Planned: reset progression with confirmation |

Commands marked planned are not active in this scaffold. `info` is the only version/build information command; there is no separate `version` command.

## Loot design

- Mob-specific weighted loot, not guaranteed OP items.
- Common food/materials remain frequent.
- Enchanted equipment is less frequent.
- Ultra-rare custom gear and ability items are exceptional.
- Bosses have stronger pools but no guaranteed named OP item.
- Item count, stack sizes, occupied slots and enchantment rolls vary.
- Never guarantee a full armour set from one chest.
- Custom rank levels 30–60 are separate from vanilla enchantment levels.
- Cooldowns, caps and multiplayer-safe ability handling are required.

## Development status

| Feature | Status |
|---|---|
| Gradle and plugin entry point | Scaffolded |
| `info`, `help`, `status`, `reload` | Implemented foundation |
| Config/messages | Scaffolded |
| Mob interaction state machine | Planned |
| Split-body model/animation | Planned |
| One-time persisted loot GUI | Planned |
| All vanilla mob loot tables | Planned |
| Custom items/enchantments/abilities | Planned |
| Legend and boss progression | Planned |
| Multiplayer persistence/anti-exploit | Planned |
| Build and gameplay integration tests | Pending |

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
