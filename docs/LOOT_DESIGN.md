# Loot Design

## Implemented generator

The Paper plugin contains mob-family common pools for passive, undead, skeleton, spider, creeper, Nether, aquatic, End and boss families. Only adult Cows and Creepers currently expose the two-click visual interaction.

Each chest rolls a variable 4–8 occupied stacks by default. Rarity weights are configurable and normalized:

| Rarity | Default weight |
|---|---:|
| Common | 70.0 |
| Uncommon | 20.0 |
| Rare | 7.0 |
| Epic | 2.0 |
| Legendary | 0.8 |
| Mythic | 0.2 |

Boss mobs get a modest rarity bias, never a guaranteed named reward.

## Rare custom rewards

- Golden Potato, Golden Porkchop and Golden Bread: 6% default special-food chance per chest.
- Liquid Gold Bucket placeholder: 7% default chance per chest, configurable between 0 and 100%.
- Custom equipment: 10% default chance, at most one piece per chest.
- Emerald Armor is currently a single randomly selected green-dyed leather armor piece, not a complete set.
- Other custom IDs include Skybound Boots, Aetherguard Chestplate, Phantom Leggings, Stormcaller Bow, Infernal Edge, Voidwalker Helmet, Skull Scepter, Abyssal Heart, Warden Echo Relic, Dragon Wing Plate and Dragon Blast.
- Custom enchantment tome metadata uses independent ranks 30–60; these are not vanilla enchantment levels.

A chest opening also ejects one or two real reward stacks into the world. Ejected stacks are cleared from the GUI inventory before spawning, so the same stack cannot be collected twice. Remaining stacks stay in the one-time GUI.

## Liquid Gold implementation status

The current Liquid Gold Bucket is a custom item placeholder with its own texture and PDC fluid ID. It intentionally uses an ordinary empty-bucket base item until the custom-fluid interaction is implemented, so it cannot accidentally place normal water.

Still required: a gold-coloured water-like fluid renderer/simulation, bucket placement and scooping, flow updates, two-source infinite-source behaviour, and the requested 10-second abilities. The exact ability list must be based on the effect screenshot.

## Remaining loot work

- Crash-safe serialization of the generated inventory and ejected-item delivery.
- Complete per-mob balance and data-pack-driven overrides.
- Functional item abilities and cooldowns.
- Functional custom enchantment effects.
