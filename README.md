# Mob Chests — Mobs Are OP Chests

**Paper 26.3 plugin | Java 25 | Liquid Gold companion resource pack**

## Components

- **Server:** install the Paper plugin JAR only. No Fabric API, client gameplay mod or datapack is required.
- **Client visual pack:** install the companion Liquid Gold resource pack to see the pale-gold bucket and animated gold fluid surface. Without it, plugin gameplay still runs, but the custom golden texture is not visible.

## Liquid Gold

Liquid Gold is a separate custom fluid simulation, not ordinary lava and not a reskinned lava block.

- Its filled bucket has a pale yellow/golden icon, distinct from the orange-red Lava Bucket.
- Right-click a block with the bucket to pour Liquid Gold into the adjacent space.
- The fluid uses vanilla water blocks underneath, so players can swim and dive through it. The plugin cancels vanilla water spreading and calculates the gold fluid's own flow.
- Right-click a Liquid Gold **source** with an empty Bucket to collect it. Flowing cells cannot be collected.
- Two source blocks placed diagonally opposite in a supported 2×2 square create the other two source blocks, producing a renewable four-source pool.
- Right-click with the bucket in the air (not at a block) to drink it, like a milk bucket. It returns an empty bucket.
- Entering or diving into the pool also grants its powers.
- Drinking or entering grants Regeneration, Speed, Strength, Resistance, Fire Resistance, Water Breathing, Haste, Jump Boost, Absorption, Night Vision, Slow Falling, Dolphin's Grace and Conduit Power for 10 seconds.
- Source and flow cells are tracked with persistent ItemDisplay metadata so they can be rediscovered when loaded chunks return.

The custom visuals use an optional 26.3 resource pack with pack format **97.1**. It changes only the Liquid Gold bucket/fluid models; vanilla Lava and Water textures remain unchanged.

### Important Liquid Gold note

The plugin simulates flow and infinite sources using water-backed blocks. It is not a new native Minecraft block-state fluid registered inside the game engine. The water block supplies collision/swimming; the plugin manages placement, flow, collection, effects and source regeneration. Flow and visuals need live-client testing on the target server.

## Mob chest interaction

1. Mob starts with normal vanilla behaviour.
2. First right-click freezes the Mob in place with a very subtle tremble. It does not sit, become tamed or become a pet.
3. Second right-click applies a smooth vanilla entity-pitch animation toward approximately -88 degrees. The real Mob keeps its normal dimensions; AI/look goals are disabled while the chest is open so the pose does not flicker up/down.
4. A chest/firework sound and particles play. Larger Mobs have a slightly louder opening sound than small animals.
5. Half of the generated loot stacks (rounded up) pop upward with light horizontal scatter, targeting about 3–4 blocks from the Mob.
6. The other half stays in one inventory GUI. Burst stacks are removed from the GUI before being spawned, so there is no duplicate.
7. Closing the GUI after taking all, some or none of the items consumes that chest permanently.
8. On a used Mob's death, only remaining saved chest items replace its normal item drops. Normal vanilla XP remains.

This is a vanilla pitch pose, not a true split-body chest hinge. Different Mob renderers may interpret pitch differently; Paper's public API does not expose independent rotation for every head, abdomen, leg or squid tentacle.

## Loot variety

Each chest generates 14–18 base occupied stacks plus separately rolled special rewards. Current pools include:
- **Blocks and stone:** stone, dirt, gravel, sand, granite, diorite, andesite, tuff, calcite, deepslate, cobbled deepslate, moss, mud, glass and dimension-themed blocks.
- **Tools:** wooden/stone starter tools in common loot; iron tools, crossbows, fishing rods, shears, spyglasses, brushes, shields, goat horns and recovery compasses in uncommon loot.
- **Materials:** bones, bone meal, ink sacs, glow ink sacs, dyes, lapis, redstone, metals, seeds and crops.
- **Utility/decor:** paper, compasses, clocks, books, chains, lanterns, candles, barrels, name tags, leads, saddles and item frames.
- **Books and potions:** signed written books, normal anvil-compatible Vanilla Enchanted Books, potions, splash potions and lingering potions.
- **Armor trims:** templates moved to the rare tier; when rolled, one template is given, never a pile.
- **Maps:** only Explorer/Trader-type pools, and one map per reward.
- **Liquid Gold Bucket:** 8% rare reward chance. It is special and not included in every mob.
- **Dimension progression:** Netherite scrap/ancient debris/ingot only in Nether rare pools; Overworld and End have their own pools.

## Custom Enchantment Books

Ancient Enchanted Books store an enchantment ID and bounded custom level in plugin metadata, not unsafe Vanilla StoredEnchantments. This avoids the untranslated enchantment.level.46 line.

**How to apply:** hold the Ancient Enchanted Book in the main hand, put compatible gear in the off-hand, then right-click. The plugin blocks the off-hand's normal equip/use action, validates gear compatibility and enchantment conflicts, applies the level, and consumes one book only on success.

Ordinary Vanilla Enchanted Books are separate loot and remain anvil-compatible.

## Requirements and installation

| Component | Target |
|---|---|
| Minecraft Java | 26.3 |
| Server | Paper 26.3 |
| Java | 25 |
| Server plugin | Required |
| Client gameplay mod | Not required |
| Datapack | Not required |
| Liquid Gold Resource Pack | Required for gold visuals |

1. Stop the Paper server.
2. Put the new plugin JAR in the server's plugins folder.
3. Start Paper 26.3 using Java 25.
4. Download the companion MobChests-Liquid-Gold-ResourcePack.zip from the same build artifact.
5. Put that ZIP in the client's .minecraft/resourcepacks folder and enable it in Options → Resource Packs. It must not be nested inside another ZIP or folder.
6. Run /mobchests info and /mobchests status.

The resource pack can also be hosted and assigned through the server's normal resource-pack setting, but manual client installation is sufficient for testing.

## Build

~~~bash
gradle clean build
python3 resource-pack/build_pack.py
~~~

The GitHub Actions artifact contains the Paper plugin JAR and the separate resource pack ZIP.

## Commands

| Command | Permission | Purpose |
|---|---|---|
| /mobchests info | Everyone | Version and creator information |
| /mobchests help | Everyone | Command guide |
| /mobchests status | Everyone | Runtime and Mob coverage status |
| /mobchests reload | mobchests.admin | Reload configuration |

## Further documentation

- Architecture
- Loot design
- Enchantment books
- Resource pack
- Testing checklist

## License

MIT — see LICENSE.
