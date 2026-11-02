# Testing Checklist

## Build/package checks
- [ ] Java 25 toolchain resolves.
- [ ] Paper 26.3 API dependency resolves.
- [ ] Root Gradle clean build succeeds.
- [ ] JAR contains plugin.yml, config.yml and messages.yml.
- [ ] Datapack/resource-pack ZIPs have pack.mcmeta at archive root.
- [ ] Every resource-pack JSON and pack.mcmeta parses.

## Single-world setup
- [ ] Start one local Paper 26.3 server with one world and one player.
- [ ] Confirm plugin enables without Multiverse or proxy dependencies.
- [ ] Confirm the resource pack loads on the client.

## Cow interaction
- [ ] Adult Cow first right-click freezes it without sitting/taming.
- [ ] Frozen Cow remains anchored if pushed or hit by knockback.
- [ ] Second right-click starts exactly one opening animation.
- [ ] Upper body rotates and lower body remains fixed.
- [ ] Original Cow model is hidden during the open session.
- [ ] Inventory appears after configured opening ticks.
- [ ] Closing after taking all items, some items or none restores the Cow.
- [ ] Chest cannot reopen after close.
- [ ] Cow returns to normal AI and can be killed with normal drops/XP.

## Failure and cleanup paths
- [ ] Player quits during opening and while GUI is open.
- [ ] Cow dies before opening.
- [ ] Chunk unload during freeze/open/GUI/closing.
- [ ] Chunk reload restores a frozen Cow or consumes interrupted opening.
- [ ] Plugin disable/reload removes temporary display entities.
- [ ] Server restart does not leave invisible entities or orphan displays.
- [ ] Two players click the same Cow at the same time; only one session wins.

## Loot
- [ ] Item count varies within configured limits.
- [ ] Stack sizes vary and obey item max-stack limits.
- [ ] Common items appear more often than mythic items.
- [ ] Enchanted gear uses vanilla enchantments.
- [ ] Custom tome rank metadata stays in 30–60.
- [ ] Custom equipment chance never creates more than one equipment piece.
- [ ] Boss bias improves the distribution without guaranteeing a particular item.
- [ ] Check all mob-family profiles against 26.3 entity names.

## Visual QA
- [ ] Verify alignment from front, sides and rear.
- [ ] No gap/overlap at the middle seam.
- [ ] No clipping during lid rotation.
- [ ] No floating display parts after every close/failure path.
- [ ] Every client sees the same display animation.

## Not yet implemented / not testable
- Custom item abilities and custom enchantment effects.
- Legend points and boss unlock progression.
- Additional mob split-body models and complete datapack-driven loot tables.
