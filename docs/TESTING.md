# Testing Checklist

## Build
- [ ] Java 25 and Paper 26.3 API resolve.
- [ ] Gradle clean build succeeds.
- [ ] JAR contains plugin.yml, config.yml, messages.yml and LiquidGoldService class.
- [ ] Resource Pack format is 97.1; all model JSON and texture PNG files validate.
- [ ] Resource Pack ZIP has pack.mcmeta at its root.
- [ ] No datapack or client gameplay mod is required.

## Liquid Gold
- [ ] Enable the companion resource pack: Gold Bucket is pale yellow/gold, visibly distinct from Lava Bucket.
- [ ] Normal Lava and Water keep their original vanilla textures.
- [ ] Right-click a solid block with Liquid Gold: it places a gold-fluid source in the adjacent space.
- [ ] Right-click air with Liquid Gold: it is consumed like milk and returns an empty bucket.
- [ ] Drink powers: Regeneration, Speed, Strength, Resistance, Fire Resistance, Water Breathing, Haste, Jump Boost, Absorption, Night Vision, Slow Falling, Dolphin's Grace and Conduit Power last 10 seconds.
- [ ] Enter/dive into the pool: the same powers activate and refresh while immersed.
- [ ] Empty bucket on a source collects a Liquid Gold Bucket.
- [ ] Empty bucket on a flowing cell does not collect it.
- [ ] Two diagonal source blocks in a supported 2×2 square create the other two sources.
- [ ] Remove one source from a 2×2 infinite pool; the remaining diagonal pair creates it again.
- [ ] Ordinary Water and Lava do not merge with or spread through Liquid Gold.
- [ ] Region protection prevents placing Liquid Gold where block placement is disallowed.
- [ ] Flow stops/continues correctly after chunk unload/reload and server restart.
- [ ] Liquid Gold drop chance remains rare (8% default).

## Mob pose and freeze
- [ ] /mobchests status reports enabled Mob EntityTypes out of the total Paper Mob EntityTypes.
- [ ] First right-click freezes the Mob in place with a subtle tremble.
- [ ] AI and awareness remain disabled through FROZEN, OPENING, LOOT_OPEN and CLOSING.
- [ ] Second right-click raises pitch toward -88 degrees.
- [ ] Open pitch remains fixed while GUI is open; it does not alternate up/down.
- [ ] Close returns saved yaw/pitch, AI, awareness, gravity and collision.
- [ ] Mob size, width, height and hitbox remain vanilla.
- [ ] Small animals have a quieter chest/firework opening sound than large Mobs.
- [ ] Creeper cannot ignite/explode while its chest is frozen or active; normal fuse behaviour returns after close.

## Enchantment books
- [ ] Custom tome in main hand plus helmet in off-hand applies Respiration.
- [ ] Custom tome in main hand plus boots in off-hand applies Feather Falling.
- [ ] Bow accepts Power; sword accepts Sharpness; crossbow accepts Quick Charge/Piercing.
- [ ] Incompatible gear, conflicts and already-higher levels are rejected without consuming the book.
- [ ] Off-hand armour/tool does not equip or move while a custom book is used.
- [ ] Normal Vanilla Enchanted Books still work in an anvil.

## Loot and persistence
- [ ] Loot count is 14–18 base occupied stacks.
- [ ] Common loot includes starter tools, building blocks, dyes, bones, ink, food and utility items.
- [ ] Uncommon loot includes iron tools, Crossbows, spyglasses, brushes, goat horns, recovery compasses and normal enchanted books.
- [ ] Armor Trim Smithing Templates are rare and arrive one at a time.
- [ ] Compasses/Maps are not given as multiple-item stacks; maps are explorer-only.
- [ ] Netherite scrap/ancient debris/ingot do not appear in Overworld or End pools.
- [ ] Half the occupied stacks (rounded up) burst from the chest; remainder stays in the GUI.
- [ ] Burst items land within roughly 3–4 blocks.
- [ ] Kill a chest-used Mob: only remaining chest contents drop, with normal XP preserved.
- [ ] Kill a Mob whose chest was never opened: normal vanilla drops and XP remain.

## Known visual limitations
- [ ] Check head/front pose on Cow, Pig, Sheep, Goat, Creeper, Zombie, Skeleton, Horse, Donkey, Parrot and Iron Golem.
- [ ] Squid tentacles require a separate renderer.
- [ ] Visual appearance and multiplayer synchronization require live Paper 26.3 client testing; CI cannot certify these.
