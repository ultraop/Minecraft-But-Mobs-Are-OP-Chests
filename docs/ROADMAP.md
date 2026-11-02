# Development Roadmap

## Phase 0 — Paper plugin foundation
- [x] Paper 26.3 / Java 25 target and CI build.
- [x] Commands, config and plugin lifecycle foundations.
- [x] Remove runtime dependency on the broken resource-pack visual model path.

## Phase 1 — Resource-pack-free mob chest lifecycle
- [x] Generic right-click handling for configured Mob entities.
- [x] Freeze in place with subtle vanilla pitch tremble.
- [x] Keep native mob size and original mob visible.
- [x] Vanilla entity-pitch open and close interpolation.
- [x] Creeper fuse protection while frozen/opening/open/closing.
- [x] One inventory session per mob.
- [x] Chest sound, firework sound and vanilla particles.
- [x] One or two real item stacks burst out and are removed from the GUI.
- [x] Restore saved AI, awareness, gravity, visibility, collision and rotation.
- [ ] Real client tests for head/front pose on each mob family.
- [ ] Separate squid tentacle/complex model-part animation (requires client renderer or custom model).

## Phase 2 — Persistent remaining loot
- [x] Serialize chest contents into Mob PersistentDataContainer.
- [x] Refresh saved inventory after inventory clicks, drags, close and player quit.
- [x] When a used mob dies, suppress vanilla item drops and XP.
- [x] Drop only the remaining saved chest items on mob death.
- [x] Leave vanilla drops intact if the chest was never opened.
- [ ] Server crash, player disconnect and two-player stress tests.

## Phase 3 — Dimension-aware loot
- [x] Broad mob-family common pools and utility items (paper, compass, map, clock, book, etc.).
- [x] Overworld progression supplies (obsidian, flint and steel, navigation/crafting items).
- [x] Nether progression supplies, with Netherite scrap/ancient debris/ingot in rare rewards.
- [x] End progression supplies and rare End loot.
- [x] Golden food IDs/names and green Emerald Armor pieces.
- [ ] Further balancing using real drop-frequency simulation.
- [ ] Persist generated loot for restart-safe GUI restoration if inventory is still open.

## Phase 4 — Custom fluid and powers
- [ ] True Liquid Gold water-like fluid placement, flow, scooping and two-source infinite behaviour.
- [ ] Implement its ten-second powers after the exact effect list is supplied.
- [ ] Custom item abilities, custom enchantment effects, Legend and boss progression.
