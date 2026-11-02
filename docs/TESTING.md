# Testing Checklist

## Build
- [ ] Java 25 and Paper 26.3 API resolve.
- [ ] Gradle clean build succeeds.
- [ ] JAR contains plugin.yml, config.yml, messages.yml and expected classes.
- [ ] No resource-pack or client-mod dependency is loaded.

## Interaction and vanilla model
- [ ] Right-click any configured Mob once: it freezes in place.
- [ ] Freeze tremble is subtle and does not change body size.
- [ ] Second right-click: pitch interpolates upward toward the configured opening angle.
- [ ] Native mob model remains visible throughout.
- [ ] Mob's size, width and height remain vanilla.
- [ ] Close GUI: pitch, AI, awareness, gravity and collision restore.
- [ ] Creeper cannot ignite/explode while chest is frozen or active; normal fuse behavior returns after close.
- [ ] One Mob cannot open a second chest after its first session is consumed.

## Loot and persistence
- [ ] Loot count and quantities vary.
- [ ] Common utility items include paper, compass, maps, clocks, books and crafting supplies.
- [ ] Overworld loot can include obsidian and flint and steel.
- [ ] Nether loot can include blaze rods and rare Netherite progression items.
- [ ] Netherite scrap/ancient debris/ingot do not appear in Overworld or End pools.
- [ ] End pools include Ender and Shulker progression supplies.
- [ ] Loot burst stacks are removed from the GUI before dropping in the world.
- [ ] Inventory click/shift-click/drag/close saves remaining GUI contents.
- [ ] Kill a chest-used mob: only remaining chest contents drop, with no vanilla mob items and no vanilla death XP.
- [ ] Kill a mob whose chest was never opened: normal vanilla drops/XP remain.
- [ ] Kill after taking all items: no chest items drop.
- [ ] Restart/chunk unload/disconnect does not duplicate stored items.

## Known visual limitations to verify
- [ ] Pitch looks like a head/front lift for Cow, Pig, Sheep, Goat, Creeper, Zombie, Skeleton, Horse, etc.
- [ ] Models that have no independent head (e.g. Squid) are documented if the whole model pitches rather than only tentacles.
- [ ] No custom resource pack is loaded or required.
- [ ] All visual checks are tested from multiple client angles and with two players.
