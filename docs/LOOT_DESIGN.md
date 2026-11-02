# Loot Design

## Implemented generator

The Paper plugin contains a mob-family loot generator. The current Cow interaction calls it; common loot profiles are included for passive, undead, skeleton, spider, creeper, Nether, aquatic, End and boss families. Additional mob interactions remain disabled until their split-body visual models pass client QA.

Each chest rolls a variable 4–8 occupied slots by default, with independently variable stack amounts. The configurable rarity weights are:

| Rarity | Default weight |
|---|---:|
| Common | 70.0 |
| Uncommon | 20.0 |
| Rare | 7.0 |
| Epic | 2.0 |
| Legendary | 0.8 |
| Mythic | 0.2 |

The generator normalizes the weights, so they do not need to total exactly 100. Boss mobs receive a small rarity bias, never a guaranteed named reward.

## Enchantments and custom items

Rare pools can contain vanilla-enchanted gear. Custom enchantment tomes store a separate rank from 30–60 in item metadata. Custom artifacts and equipment store stable item IDs. These identifiers are foundations for future abilities; custom item abilities and custom enchantment effects are not active in this build.

The custom equipment roll defaults to 10% and replaces an existing loot slot. A chest can therefore contain at most one custom equipment piece, never a guaranteed full armour set.

Named custom equipment includes Skybound Boots, Aetherguard Chestplate, Phantom Leggings, Stormcaller Bow, Infernal Edge and Voidwalker Helmet. Named artifacts include Liquid Gold Bucket, Skull Scepter, Abyssal Heart, Warden Echo Relic, Dragon Wing Plate and Dragon Blast.

## Remaining loot work

- Per-entity persisted serialized reward contents and crash-safe delivery semantics.
- Fine-grained per-mob weights and complete 26.3 entity coverage.
- Functional custom-item abilities, custom enchantment effects and cooldowns.
- Data-pack-driven overrides and admin preview/test commands.
