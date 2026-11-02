# Development Roadmap

## Phase 0 — Repository/build foundation
- [x] Repository structure, Paper 26.3 / Java 25 target and CI build.
- [x] Main info, help, status and reload command foundations.

## Phase 1 — Cow interaction lifecycle
- [x] First right-click freezes an adult Cow without taming/sitting.
- [x] Second right-click starts the one-time open animation.
- [x] Persistent entity state and freeze anchor.
- [x] One GUI session; closing after taking all/some/none consumes the chest.
- [x] Restore original AI, awareness, gravity, visibility and collision.
- [x] Preserve ordinary vanilla death drops and XP.
- [x] Chunk load/unload and plugin enable/disable recovery.
- [ ] Real Paper 26.3 in-game tests and two-player race/quit tests.

## Phase 2 — Visual proof of concept
- [x] Cow upper/lower ItemDisplay parts and configurable interpolation duration.
- [x] Pack metadata and texture/model generation.
- [ ] Real-client seam, rotation, clipping, orientation and cleanup verification.
- [ ] Create and test individual models for each additional supported mob.
- [ ] Verify all clients see the same animation.
- [ ] Consider Fabric renderer integration if the Paper display illusion cannot meet the target.

## Phase 3 — Loot engine
- [x] Mob-family common loot profiles.
- [x] Variable slots/stack quantities and weighted rarity.
- [x] Vanilla enchanted gear pool.
- [x] Custom rank 30–60 tome metadata and stable custom item IDs.
- [x] One custom equipment piece maximum per chest.
- [x] Boss rarity bias without guaranteed named rewards.
- [ ] Full mob-by-mob balancing and datapack-driven overrides.
- [ ] Persist serialized result contents and test interruption/restart delivery semantics.

## Phase 4 — Custom equipment and abilities
- [ ] Implement item abilities, cooldowns, anti-stacking and multiplayer-safe event handlers.
- [ ] Implement custom enchantment effects for ranks 30–60.
- [ ] Add custom item models and sounds.

## Phase 5 — Legend and bosses
- [ ] Legend points, discoveries and per-player progression.
- [ ] Boss unlock states, summon/spawn systems and special boss reward flow.

## Phase 6 — Release gate
- [ ] Build and static validation.
- [ ] Paper 26.3 server smoke test.
- [ ] Resource-pack client test with screenshots/video.
- [ ] Two-player multiplayer test.
- [ ] Persistence/restart validation.
- [ ] Final release artifact after all gates pass.
