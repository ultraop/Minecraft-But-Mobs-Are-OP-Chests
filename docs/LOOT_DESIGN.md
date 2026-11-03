# Loot Design

## Loot quantity and variety

Each chest rolls 14–18 base occupied stacks plus separate special rewards. Quantities vary by material stack size. Common and uncommon pools mix Mob-family loot with survival items: stone, dirt, gravel, sand, stone variants, deepslate, dyes, bones, bone meal, ink sacs, crops, metals, tools, signed written books, potions, lanterns and utility items.

- Common tools include wooden/stone tools, fishing rods, shears, flint and steel, shields and utility supplies.
- Uncommon tools include iron tools, crossbows, spyglasses, brushes, goat horns, recovery compasses and normal Enchanted Books.
- Armor Trim Smithing Templates are removed from the broad uncommon list and rolled only in the Rare tier. A trim reward is one template.
- Maps are limited to explorer/trader-type Mob pools and are always one item.
- Compass and recovery compass rewards are one item, not stacked piles.

## Dimension-aware progression

| Dimension | Progression examples |
|---|---|
| Overworld | Obsidian, flint and steel, iron, building blocks and crafting supplies |
| Nether | Quartz, glowstone dust, blaze rods, gold, Nether bricks, with ancient debris and Netherite scrap/ingot in rare pools |
| End | Ender pearls, chorus fruit, purpur, End Stone, Shulker shells and Dragon-related rare materials |

Netherite progression loot is limited to the Nether environment. Overworld and End pools do not include Netherite scrap, ancient debris or Netherite ingots.

## Rarity and special rewards

- Liquid Gold Bucket: 8% rare special drop chance by default.
- Golden foods: configurable special-food chance.
- Custom equipment: configurable chance, at most one piece per chest.
- Emerald Armor is one green-dyed leather armour piece, not a complete set.
- Ancient Enchanted Books use real vanilla enchantment keys and bounded custom levels.
- Normal Vanilla Enchanted Books are separate rewards and use standard stored enchantment levels, so they remain anvil-compatible.

## Burst and death-drop rule

At chest opening, 50% of all occupied GUI stacks (rounded up) are removed from the GUI before being launched upward with light horizontal scatter. The remaining stacks stay in the GUI.

The live GUI contents are serialized into the Mob's PersistentDataContainer. After a Mob's chest has been used, killing that Mob clears vanilla item drops and drops only the remaining serialized chest contents. Normal XP is preserved. If the chest was never opened, ordinary vanilla mob drops and XP remain.
