# Custom Items

## Current item foundations

| Item | Current implementation |
|---|---|
| Golden Potato | Custom name and metadata on a baked potato |
| Golden Porkchop | Custom name and metadata on a cooked porkchop |
| Golden Bread | Custom name and metadata on bread |
| Emerald Armor | One green-dyed leather armour piece per equipment reward; never a full set |
| Liquid Gold Bucket | Sealed placeholder with a lava-bucket-style icon; right-click is blocked so it cannot place ordinary lava |
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

The current no-resource-pack build uses vanilla textures. Custom item models/textures are not included.

## Custom Enchantment Books

Ancient Enchanted Books are functional plugin-applied books. Their enchantment ID and bounded custom level are stored in PersistentDataContainer, not unsafe high-level Vanilla StoredEnchantments. Hold compatible gear in the off-hand and the book in the main hand, then right-click. The plugin checks compatibility and conflicts, applies the native vanilla enchantment to the gear and consumes the book only on success.

Custom levels are enchantment-specific and bounded (for example, Sharpness 8–12, Respiration 4–6, Feather Falling 6–8). See [Enchantment Books](ENCHANTMENT_BOOKS.md) for the complete ranges and gear rules. Ordinary Vanilla Enchanted Books are separate loot and remain anvil-compatible.

## Planned abilities

The item names above are loot foundations. Their individual abilities, cooldowns, anti-stacking rules, Legend points and boss progression are not implemented yet.

Liquid Gold still requires a true golden water-like fluid renderer/simulation, bucket placement and scooping, flow updates, two-source infinite behaviour and its requested ten-second status effects.
