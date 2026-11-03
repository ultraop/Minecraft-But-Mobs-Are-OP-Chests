# Development Roadmap

## Phase 0 — Paper plugin foundation
- [x] Paper 26.3 / Java 25 target and CI build.
- [x] Commands, config and plugin lifecycle.
- [x] Remove runtime dependency on resource-pack models.
- [x] Remove unused resource-pack and datapack placeholders from the repository.

## Phase 1 — Resource-pack-free mob chest lifecycle
- [x] Generic right-click handling for configured Mob entities.
- [x] Freeze in place with subtle pitch tremble.
- [x] Keep native Mob size, width, height and model.
- [x] Apply a fixed vanilla pitch animation while AI/look goals are disabled.
- [x] Hold the open pose for the entire GUI session; restore the original rotation on close.
- [x] Creeper fuse protection while frozen/opening/open/closing.
- [x] One inventory session per Mob.
- [x] Chest sound, firework sound and vanilla particles.
- [x] Eject 50% of occupied loot stacks (rounded up) with a short 3–4 block scatter target.
- [x] Remove burst stacks from the GUI before spawning to prevent duplicates.
- [x] Restore saved AI, awareness, gravity, visibility, collision and rotation.
- [ ] Live client tests for Cow, Pig, Sheep, Goat, Creeper, Zombie, Skeleton, Horse, Donkey, Parrot and Iron Golem.
- [ ] Separate squid tentacle/complex model-part animation (requires a custom renderer).

## Phase 2 — Persistent remaining loot
- [x] Serialize chest contents into Mob PersistentDataContainer.
- [x] Refresh saved inventory after clicks, drags, close and player quit.
- [x] When a used Mob dies, replace vanilla item drops with the remaining chest contents.
- [x] Preserve normal vanilla XP.
- [x] Leave vanilla drops and XP intact if the chest was never opened.
- [ ] Server crash, player disconnect and two-player stress tests.

## Phase 3 — Dimension-aware loot
- [x] 14–18 base occupied stacks plus separate special reward rolls.
- [x] Broad Mob-family loot and mixed survival items: blocks, stone, dirt, bones, ink, dyes, food, utility, books, potions and decor.
- [x] Signed written books and valid vanilla enchanted books.
- [x] Explorer/trader-only map rewards rather than a map in every Mob pool.
- [x] Overworld progression supplies including obsidian and flint and steel.
- [x] Nether progression supplies, with Netherite scrap/ancient debris/ingot in rare rewards.
- [x] End progression supplies.
- [x] Golden food IDs/names and single-piece green Emerald Armor rewards.
- [ ] Further balancing using real drop-frequency simulation.

## Phase 4 — Enchantments and custom items
- [x] Custom Ancient Enchanted Books store a PDC enchantment ID and bounded level, not unsafe Vanilla StoredEnchantments.
- [x] Apply custom books from main hand to compatible off-hand gear with explicit compatibility/conflict checks.
- [x] Migrate old high-level custom books, remove legacy stored levels and clamp to each enchantment's safe maximum.
- [x] Keep ordinary Vanilla Enchanted Books separate and anvil-compatible.
- [ ] Live in-game tests for every enchantment and gear type.
- [ ] Liquid Gold fluid flow/infinite source and requested ten-second powers.
- [ ] Custom artifact abilities, cooldowns, Legend points and boss progression.

## Phase 5 — Release
- [ ] Live Paper 26.3 server smoke test.
- [ ] Mob pose visual QA and two-player synchronization.
- [ ] Enchantment application and retained-effect tests on real gear.
- [ ] Loot burst radius and death-drop validation.
