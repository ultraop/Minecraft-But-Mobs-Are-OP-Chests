# Testing Checklist

## Build
- [ ] Java 25 and Paper 26.3 API resolve.
- [ ] Gradle clean build succeeds.
- [ ] JAR contains plugin.yml, config.yml, messages.yml and expected classes.
- [ ] No resource-pack or client-mod dependency is loaded.
- [ ] No obsolete Cow/Creeper ItemDisplay animator classes are bundled.

## Mob pose and freeze
- [ ] First right-click freezes the Mob in place with a subtle tremble.
- [ ] AI and awareness remain disabled through FROZEN, OPENING, LOOT_OPEN and CLOSING.
- [ ] Second right-click raises pitch toward -88 degrees.
- [ ] Open pitch remains fixed while GUI is open; it does not alternate up/down.
- [ ] Close returns saved yaw/pitch, AI, awareness, gravity and collision.
- [ ] Mob size, width, height and hitbox remain vanilla.
- [ ] Creeper cannot ignite/explode while its chest is frozen or active; normal fuse behaviour returns after close.

## Enchantment books
- [ ] Right-click in air works even when vanilla would mark PlayerInteractEvent as cancelled.
- [ ] Right-click on a block works without opening/using the block.
- [ ] Custom tome in main hand plus helmet in off-hand applies Respiration.
- [ ] Custom tome in main hand plus boots in off-hand applies Feather Falling.
- [ ] Bow in off-hand accepts Power; sword accepts Sharpness; crossbow accepts Quick Charge/Piercing.
- [ ] Respiration on leggings and Feather Falling on a helmet are rejected without consuming the book.
- [ ] Off-hand armour/tool does not equip or move while the custom book is used.
- [ ] Conflicting enchantments and already-higher levels are rejected without consuming the book.
- [ ] Successful application consumes one custom book and the item retains its enchantment after relog/restart.
- [ ] Custom book tooltip has no untranslated enchantment.level.N line.
- [ ] Normal Vanilla Enchanted Books still work in an anvil.
- [ ] Old custom books with stored high levels have their legacy stored entry removed and applied level clamped to the enchantment-specific maximum (Respiration 6, Sharpness 12, Mending 1).

## Loot and persistence
- [ ] Loot count is 14–18 base occupied stacks plus any special rewards.
- [ ] Common pools mix stone, dirt, sand, gravel, deepslate, dyes, bones, ink, food, crafting materials and utility items.
- [ ] Signed Written Books, normal Vanilla Enchanted Books and effect-bearing potions are valid items.
- [ ] Common loot includes wooden/stone tools, fishing rods, shears, flint and steel, shields and compasses.
- [ ] Uncommon loot includes iron tools, crossbows, spyglasses, brushes, goat horns and recovery compasses.
- [ ] Normal Vanilla Enchanted Books have valid stored levels.
- [ ] Maps are restricted to explorer/trader-type Mob pools.
- [ ] Netherite scrap/ancient debris/ingot do not appear in Overworld or End pools.
- [ ] Half the occupied stacks (rounded up) are burst from the chest; the remainder stays in the GUI.
- [ ] Burst items land within the intended 3–4 block area and are removed from the GUI before spawning.
- [ ] Inventory click/shift-click/drag/close saves remaining GUI contents.
- [ ] Kill a chest-used Mob: only remaining chest contents drop, with normal XP preserved.
- [ ] Kill a Mob whose chest was never opened: normal vanilla drops and XP remain.
- [ ] Kill after taking all items: no chest items drop.

## Known visual limitations
- [ ] Check head/front pose on Cow, Pig, Sheep, Goat, Creeper, Zombie, Skeleton, Horse, Donkey, Parrot and Iron Golem.
- [ ] Squid tentacles are documented as requiring a separate renderer.
- [ ] Verify all checks from multiple client angles and with two players.
- [ ] Visual appearance and multiplayer synchronization require live Paper 26.3 client testing; CI cannot certify these.
