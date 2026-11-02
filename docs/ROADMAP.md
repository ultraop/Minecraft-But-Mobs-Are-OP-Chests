# Development Roadmap

## Phase 0 — Repository/build foundation
- [x] Initial repository structure.
- [x] Paper 26.3 / Java 25 target.
- [x] `/mobchests info` and `/mobchests help` foundation.
- [x] Configuration and reload foundation.
- [x] GitHub Actions Java 25 build workflow.

## Phase 1 — Cow interaction lifecycle
- [x] First right-click freezes the Cow without taming/sitting.
- [x] Second right-click starts the one-time opening flow.
- [x] Persistent per-entity state in PDC.
- [x] One inventory session per Cow.
- [x] Closing the GUI consumes the opening even if the player takes no items.
- [x] Restore original AI, awareness, gravity, visibility and collision flags.
- [x] Preserve normal Cow drops and XP after later death.
- [x] Recovery on plugin enable and chunk load.
- [x] Cleanup on death, chunk unload and plugin disable.
- [ ] Manual in-game verification on Paper 26.3 with the custom resource pack.
- [ ] Two-player race and quit-during-animation test.

## Phase 2 — Visual proof of concept
- [x] Separate Cow upper/lower ItemDisplay models.
- [x] Interpolated upper-lid rotation; fixed lower body.
- [x] 26.3 resource-pack model definitions and texture generator.
- [ ] Verify exact cow alignment, seam and no clipping in an actual client.
- [ ] Verify all viewers see the same animation.
- [ ] Confirm no visual residue after GUI close, death, chunk unload or restart.
- [ ] Adjust model geometry based on client screenshots/video.
- [ ] Only then adapt the rendering strategy to other mob shapes.

## Phase 3 — One-time loot engine
- [x] Temporary Cow-specific varied loot for the animation test.
- [ ] Data-driven loot table per vanilla mob.
- [ ] Persist generated inventory before opening.
- [ ] Variable slots, stack counts and weighted rarity.
- [ ] Prevent rerolls and duplicate item delivery across restart.
- [ ] Custom equipment probability and boss modifiers.
- [ ] Full inventory close / player disconnect / server crash tests.

## Phase 4 — Custom equipment and abilities
- [ ] Unique item names, lore and models.
- [ ] Custom enchantment ranks 30–60, separate from vanilla levels.
- [ ] Cooldowns, caps and balance configuration.
- [ ] Multiplayer-safe ability handling.

## Phase 5 — Legend and bosses
- [ ] Overworld, Ocean, Nether and End categories.
- [ ] Unique discoveries and points.
- [ ] Wither, Elder Guardian, Warden and Dragon unlock progression.
- [ ] Boss-specific high-tier loot.

## Phase 6 — Release
- [ ] Automated tests and config validation.
- [ ] Paper 26.3 server smoke test.
- [ ] Resource-pack client test.
- [ ] Two-player multiplayer test.
- [ ] Persistence/restart validation.
- [ ] Final JAR and companion pack ZIPs.
- [ ] `info` command reports only verified features.
