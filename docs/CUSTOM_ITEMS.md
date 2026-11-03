# Custom Items

## Current item foundations

| Item | Current implementation |
|---|---|
| Golden Potato | Custom name and metadata on a baked potato |
| Golden Porkchop | Custom name and metadata on a cooked porkchop |
| Golden Bread | Custom name and metadata on bread |
| Emerald Armor | One green-dyed leather armour piece per equipment reward; never a full set |
| Liquid Gold Bucket | Pale-gold custom bucket model; placeable, water-backed flow simulation, source collection, drinking and 10-second powers |
| Skybound Boots | Rare equipment ID and diamond-boots base; special jump/fall powers not implemented |
| Aetherguard Chestplate | Rare equipment ID and diamond-chestplate base; defensive pulse not implemented |
| Phantom Leggings | Rare equipment ID and diamond-leggings base; movement burst not implemented |
| Stormcaller Bow | Rare equipment ID and bow base; storm shot not implemented |
| Infernal Edge | Rare equipment ID; Netherite sword base only in Nether, Diamond sword base elsewhere |
| Voidwalker Helmet | Rare equipment ID and diamond-helmet base; detection ability not implemented |
| Skull Scepter | Rare relic ID and blaze-rod base; Wither attack not implemented |
| Abyssal Heart | Rare relic ID and heart-of-the-sea base; water aura/healing not implemented |
| Warden Echo Relic | Rare relic ID and echo-shard base; sonic attack not implemented |
| Dragon Wing Plate | Rare relic ID and elytra base; custom armour/flight ability not implemented |
| Dragon Blast | Rare relic ID and fire-charge base; special blast ability not implemented |

Other custom items use vanilla base textures. The companion resource pack provides the unique Liquid Gold Bucket and fluid visuals.

## Liquid Gold

The bucket is a separate fluid item, not a Lava Bucket:
- Right-click a block to pour it into the adjacent block space.
- Right-click a source with an empty bucket to collect it.
- Two diagonal sources in a supported 2×2 square generate the other two source cells.
- Right-click air to drink it, like milk; the filled bucket becomes an empty bucket.
- Entering the pool also activates the powers.
- Regeneration, Speed, Strength, Resistance, Fire Resistance, Water Breathing, Haste, Jump Boost, Absorption, Night Vision, Slow Falling, Dolphin's Grace and Conduit Power last 10 seconds.
- Water-backed blocks provide vanilla swimming/collision. The plugin manages the custom flow and source rules; the resource pack makes the fluid look golden.

## Custom Enchantment Books

Ancient Enchanted Books are functional plugin-applied books. Their enchantment ID and bounded custom level are stored in PersistentDataContainer, not unsafe high-level Vanilla StoredEnchantments. Hold compatible gear in the off-hand and the book in the main hand, then right-click. The plugin checks compatibility and conflicts, applies the native vanilla enchantment to the gear and consumes the book only on success.

Custom levels are enchantment-specific and bounded. Ordinary Vanilla Enchanted Books are separate loot and remain anvil-compatible.

## Planned abilities

The equipment/relic names above are loot foundations. Their individual powers, cooldowns, anti-stacking rules, Legend points and boss progression are not implemented yet.
