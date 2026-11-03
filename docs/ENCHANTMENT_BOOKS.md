# Ancient Enchanted Books

## How the custom book works

The Ancient Enchanted Book is a plugin-applied book, not a Vanilla Enchanted Book for the anvil.

1. Put the compatible armour/tool in the off-hand.
2. Hold the Ancient Enchanted Book in the main hand.
3. Right-click air or a block.
4. The plugin blocks the off-hand's normal equip/use action, checks item compatibility and conflicts, applies the level and consumes the book only if the operation succeeds.

The custom book stores its enchantment ID and level in PersistentDataContainer. It does not add an unsafe level 30–60 to Vanilla EnchantmentStorageMeta, so it will not show the untranslated `enchantment.level.46` tooltip. Anvil use is intentionally not supported for custom books.

Normal Vanilla Enchanted Books are a separate reward. They contain standard stored enchantments and can be used in an anvil.

## Bounded custom levels

| Enchantment | Level range | Compatible item |
|---|---:|---|
| Sharpness | 8–12 | Sword / compatible weapon |
| Smite | 8–12 | Sword / compatible weapon |
| Bane of Arthropods | 8–12 | Sword / compatible weapon |
| Power | 8–12 | Bow |
| Protection | 6–8 | Armour |
| Blast Protection | 5–7 | Armour |
| Fire Protection | 5–7 | Armour |
| Projectile Protection | 5–7 | Armour |
| Efficiency | 8–10 | Pickaxe, axe, shovel or hoe |
| Unbreaking | 6–9 | Compatible gear |
| Fortune | 4–6 | Compatible tool |
| Looting | 4–6 | Sword |
| Feather Falling | 6–8 | Boots |
| Respiration | 4–6 | Helmet |
| Sweeping Edge | 4–6 | Sword |
| Fire Aspect | 2–4 | Sword |
| Knockback | 2–4 | Sword |
| Depth Strider | 4–5 | Boots |
| Thorns | 4–5 | Armour |
| Soul Speed | 4–5 | Boots |
| Swift Sneak | 4–5 | Leggings |
| Quick Charge | 3–5 | Crossbow |
| Piercing | 4–6 | Crossbow |
| Impaling | 6–8 | Trident |
| Loyalty | 3–5 | Trident |
| Riptide | 3–5 | Trident |

The plugin uses Vanilla enchantment effects at these bounded levels. Enchantments which do not scale usefully beyond level I (for example Mending) are not issued as high-level custom books; they may appear as normal Vanilla Enchanted Books instead.

## Failure-safe application

- Empty off-hand: book is not consumed.
- Incompatible item (for example Respiration on leggings): book is not consumed.
- Existing equal/higher level: book is not consumed.
- Conflicting enchantment: application is rejected and book is not consumed.
- Successful application: the item in the off-hand is updated and one book is consumed.
- Off-hand armour/tool use is cancelled during the custom-book interaction so it does not move into an equipment slot.
- Previously issued custom books with unsafe stored levels are migrated by removing their legacy StoredEnchantments entry and clamping their applied level to 15.
