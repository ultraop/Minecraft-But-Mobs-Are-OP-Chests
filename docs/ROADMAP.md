# Development Roadmap

## Phase 0 — Repository/build foundation
- [x] Paper 26.3 / Java 25 target, repository structure and CI.
- [x] Main info, help, status and reload command foundations.

## Phase 1 — Interaction lifecycle
- [x] Adult Cow first right-click freeze and subtle tremble.
- [x] Creeper first right-click freeze, subtle tremble and fuse disarm.
- [x] Cow body-split and Creeper head-lid second-click animation foundations.
- [x] One GUI session per mob; close after taking all/some/none consumes it.
- [x] Firework sound/particle opening and 1–2 real loot stacks burst upward/outward.
- [x] Restore original AI, awareness, gravity, visibility and collision.
- [x] Creeper explosion suppression during frozen/open states; normal fuse behaviour resumes after close.
- [x] Preserve ordinary vanilla death drops and XP.
- [x] Chunk/plugin recovery paths.
- [ ] Real Paper 26.3 client testing, two-player race and disconnect tests.

## Phase 2 — Visual models
- [x] Cow middle-seam and Creeper head-lid model assets.
- [x] Generated Cow/Creeper textures and custom Golden food/Bucket item art.
- [x] CI namespace, texture-reference, ZIP and y=8 seam validation.
- [ ] Real-client scale, hinge, clipping, orientation and cleanup verification.
- [ ] Add separate tested models for Zombie, Skeleton, Squid, Pig, Horse, Sheep, Goat and other mobs.
- [ ] Verify every client sees the same display animation.
- [ ] Consider Fabric renderer integration only if Paper display entities cannot meet the visual target.

## Phase 3 — Loot
- [x] Mob-family common loot profiles, variable slots and stack counts.
- [x] Weighted rarity and vanilla enchanted gear.
- [x] Rare Golden Potato, Golden Porkchop and Golden Bread.
- [x] Rare single-piece Emerald Armor reward.
- [x] Configurable Liquid Gold Bucket placeholder drop chance.
- [x] One custom equipment piece maximum per chest.
- [ ] Custom Liquid Gold fluid flow, bucket placement/scooping and two-source infinite behaviour.
- [ ] Persist serialized reward contents and test crash-safe delivery.
- [ ] Full mob-by-mob balance and data-pack-driven overrides.

## Phase 4 — Items and abilities
- [ ] Implement Liquid Gold's requested 10-second status abilities after exact effect list is supplied.
- [ ] Implement named equipment/artifact abilities, cooldowns and anti-stacking.
- [ ] Implement custom enchantment effects for ranks 30–60.
- [ ] Add remaining custom item models/sounds.

## Phase 5 — Legend and bosses
- [ ] Legend points and discoveries.
- [ ] Boss unlock states, summon/spawn systems and special boss reward flow.

## Phase 6 — Release
- [ ] Real Paper 26.3 server smoke test.
- [ ] Resource-pack visual tests and client screenshots/video.
- [ ] Two-player multiplayer and restart validation.
- [ ] Final release after all gates pass.
