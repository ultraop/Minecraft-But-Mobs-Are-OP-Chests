# Loot Design

Mob-specific weighted pools; no global random table. Common food/materials remain frequent. Useful enchanted equipment is less frequent. Ultra-rare custom gear and abilities are memorable discoveries. Bosses have stronger high-tier pools but no guaranteed named OP item. Never guarantee a full armour set from one chest. Vary occupied slots, stack sizes, item quality and enchantment rolls. Persist one result per mob.

## Provisional rarity weights
Common 70; Uncommon 20; Rare 7; Epic 2; Legendary 0.8; Mythic 0.2.

Custom equipment chance defaults to 10% as a separate reward roll, not a guaranteed item and not the same as a Mythic roll. Define roll semantics clearly to avoid multiplying chance unintentionally.

## Variable contents
- Occupied slots configurable (initial range 0–15).
- Stack sizes independently configured per item and obey item limits.
- Normally one special equipment piece at most per chest.
- Some chests can be poor or nearly empty.
- Every 26.3 vanilla mob gets a table or documented inheritance mapping.

Custom enchantment ranks 30–60 are separate from vanilla enchantment levels. Every ability needs caps, cooldowns and multiplayer-safe handling.
