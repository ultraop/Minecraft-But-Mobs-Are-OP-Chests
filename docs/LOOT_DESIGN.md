# Loot Design

## Implemented generator

The Paper plugin creates variable 5–9 base stacks by default, with variable stack sizes and configurable weighted rarity. Each Mob family has a themed common pool. General utility items are mixed in so different mobs can contain paper, compass, maps, clocks, books, arrows, metals, crafting resources and other practical supplies.

## Dimension-aware progression

The loot generator receives the Mob's current world environment.

| Dimension | Progression examples |
|---|---|
| Overworld | Obsidian, flint and steel, iron, paper, compass, map and clock |
| Nether | Quartz, glowstone dust, blaze rods, gold, magma cream, Nether bricks, with ancient debris and Netherite scrap/ingot in rare pools |
| End | Ender pearls, chorus fruit, purpur, shulker shells and Dragon-related rare materials |

Netherite progression loot is limited to the Nether environment. Overworld and End pools do not include Netherite scrap/ancient debris/ingots.

## Rarity

Default weights are configurable and normalized:

| Rarity | Default weight |
|---|---:|
| Common | 70.0 |
| Uncommon | 20.0 |
| Rare | 7.0 |
| Epic | 2.0 |
| Legendary | 0.8 |
| Mythic | 0.2 |

Boss mobs receive a small rarity bias only. No named item is guaranteed.

## Custom rewards

- Golden Potato, Golden Porkchop and Golden Bread use vanilla base items with custom names and metadata; custom item textures are not part of this no-resource-pack build.
- Emerald Armor currently uses a single green-dyed leather armor piece.
- Liquid Gold Bucket is a non-water placeholder item. Its fluid system and ten-second abilities are not implemented.
- Custom equipment is limited to one equipment reward per chest.
- Custom tome metadata stores ranks 30–60 separately from vanilla enchantment levels.
- Other named artifacts remain item-ID foundations until their abilities are implemented.

## Burst and death-drop rule

At chest opening, one or two actual loot stacks are removed from the GUI inventory before being launched into the world. The remaining GUI contents are serialized into the Mob's PersistentDataContainer and refreshed as inventory clicks, drags and closing occur.

After a Mob's chest has been used, killing that Mob clears vanilla item drops and XP and drops only the saved remaining chest contents. If the chest was never opened, ordinary vanilla mob drops remain. The ejected stacks are not duplicated in the saved inventory.

## Remaining work

- Real server crash and multiplayer stress tests around inventory checkpointing.
- More fine-grained per-Mob and biome balancing.
- Functional Liquid Gold fluid flow/infinite source and requested ten-second abilities.
- Custom item abilities, custom enchantment effects, Legend points and boss progression.
