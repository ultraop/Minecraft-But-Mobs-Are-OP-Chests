# Testing Checklist

## Build/package
- [ ] Java 25 toolchain and Paper 26.3 API resolve.
- [ ] Root Gradle clean build succeeds.
- [ ] JAR contains plugin.yml, config.yml and messages.yml.
- [ ] Datapack/resource-pack ZIPs have pack.mcmeta at archive root.
- [ ] Resource-pack validator finds all item models and texture references.
- [ ] Cow and Creeper y=8 split seams validate.
- [ ] Every pack JSON parses and ZIP testzip passes.

## Single-world server
- [ ] Start one Paper 26.3 world with Java 25.
- [ ] Plugin enables without Fabric API, Forge, ProtocolLib or other dependencies.
- [ ] Resource pack loads on the client.

## Cow
- [ ] First right-click anchors Cow and begins subtle tremble.
- [ ] Tremble is low amplitude and configurable.
- [ ] Second right-click opens at the middle seam.
- [ ] Lower body stays fixed; upper body hinges.
- [ ] No original Cow mesh remains visible behind display parts.
- [ ] One/two loot stacks burst upward; those stacks are absent from the GUI.
- [ ] Remaining GUI contents vary; close after all/some/none consumes chest.
- [ ] Cow returns to normal AI and vanilla drops/XP remain unchanged.

## Creeper
- [ ] First right-click freezes it and keeps fuse at zero / not ignited.
- [ ] Tremble is subtle and not visually distracting.
- [ ] Second right-click opens the head as a lid while the lower body stays fixed.
- [ ] Firework/chest sound and loot burst play once.
- [ ] Creeper cannot explode while frozen, opening, GUI-open or closing.
- [ ] After GUI close and animation cleanup, Creeper AI and normal fuse behaviour resume.
- [ ] After chest is used, it cannot be reopened.
- [ ] Later death still produces normal vanilla drops and XP.

## Inventory/drop safety
- [ ] Burst items are removed from the GUI before being spawned in the world.
- [ ] Burst items are real collectible Item entities, not duplicate display-only copies.
- [ ] One mob cannot open two sessions at once.
- [ ] Disconnect, death, chunk unload/reload and plugin restart clean temporary displays.
- [ ] A failed animation returns the mob to its frozen state.

## Loot
- [ ] Golden foods use custom models and IDs.
- [ ] Emerald Armor roll yields only one armor piece.
- [ ] Liquid Gold Bucket has the configured 5–10% target odds.
- [ ] Liquid Gold cannot place ordinary water while custom fluid support is pending.
- [ ] Custom equipment remains at most one piece per chest.
- [ ] Common items remain more frequent than Mythic items.

## Visual QA
- [ ] Cow seam/hinge is aligned from all sides.
- [ ] Creeper head hinge stays at the neck seam.
- [ ] No clipping while either lid rotates.
- [ ] Burst trajectory and pickup delay feel readable, not overwhelming.
- [ ] No floating ItemDisplays remain after close/failure/chunk unload.

## Not implemented in this build
- Additional mob models (Pig, Horse, Zombie, Skeleton, Squid, Sheep, Goat, etc.).
- Liquid Gold fluid placement, flow and infinite sources.
- Liquid Gold's 10-second potion/ability effects.
- Custom gear abilities, custom enchantment effects, Legend and boss progression.
