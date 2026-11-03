# Loot Design

## Loot quantity and variety

Each chest rolls 14–18 base occupied stacks plus separate special rewards. Quantities vary by material stack size. Pools combine Mob-family loot with broad survival supplies: dirt, stone, gravel, sand, stone variants, deepslate, blocks, dyes, bones, bone meal, ink sacs, crops, metals, books, signed written books, potions, chains, lanterns, utility items and smithing templates. Common loot can include wooden/stone starter tools, fishing rods, shears, flint and steel, shields and compasses. Uncommon loot adds iron tools, crossbows, spyglasses, brushes, goat horns, recovery compasses and normal Enchanted Books.

Maps are only added to explorer/trader-type Mob pools. Compasses can appear in common and uncommon utility loot rather than being forced into every chest. Creative/admin-only items, spawn eggs and restricted utility blocks are filtered from random material selection.

## Dimension-aware progression

| Dimension | Progression examples |
|---|---|
| Overworld | Obsidian, flint and steel, iron, building blocks and crafting supplies |
| Nether | Quartz, glowstone dust, blaze rods, gold, Nether bricks, with ancient debris and Netherite scrap/ingot in rare pools |
| End | Ender pearls, chorus fruit, purpur, End Stone, Shulker shells and Dragon-related rare materials |

Netherite progression loot is limited to the Nether environment. Overworld and End pools do not include Netherite scrap, ancient debris or Netherite ingots.

## Rarity and special rewards

Default rarity weights are configurable and normalized:

| Rarity | Default weight |
|---|---:|
| Common | 70.0 |
| Uncommon | 20.0 |
| Rare | 7.0 |
| Epic | 2.0 |
| Legendary | 0.8 |
| Mythic | 0.2 |

Boss mobs receive a small rarity bias only. No named item is guaranteed.

- Liquid Gold Bucket placeholder: 35% test-build chance; sealed item that does not place ordinary lava.
- Golden foods: 6% special-food chance.
- Custom equipment: 10% chance, at most one piece per chest.
- Emerald Armor is one green-dyed leather armour piece, not a complete set.
- Ancient Enchanted Books use real vanilla enchantment keys and bounded custom levels; the list and application rules are in [Enchantment Books](ENCHANTMENT_BOOKS.md).
- Normal Vanilla Enchanted Books are separate loot and use standard stored enchantment levels, so they remain anvil-compatible.

## Enchantment burst and death-drop rule

At chest opening, 50% of all occupied GUI stacks (rounded up) are removed from the GUI before they are launched outward. Their low horizontal velocity targets roughly 3–4 blocks of scatter. The remaining stacks stay in the GUI.

The live GUI contents are serialized into the Mob's PersistentDataContainer and refreshed after inventory clicks, drags and closing. After a Mob's chest has been used, killing that Mob clears vanilla item drops and drops only the remaining serialized chest contents. Normal XP is preserved. If the chest was never opened, ordinary vanilla mob drops and XP remain.

## Liquid Gold status

The current Liquid Gold Bucket is a sealed placeholder. It has a lava-bucket-style filled icon, but its right-click is blocked to prevent placing ordinary lava. A true golden water-like fluid, bucket placement/scooping, flow updates, two-source infinite behaviour and the requested ten-second abilities are not implemented yet.
