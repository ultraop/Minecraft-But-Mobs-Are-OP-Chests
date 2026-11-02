# Testing Checklist

## Build and packaging
- [ ] Java 25 toolchain resolves.
- [ ] Paper 26.3 API dependency resolves.
- [ ] Root `gradle clean build` builds the plugin subproject.
- [ ] JAR contains plugin.yml, config.yml and messages.yml.
- [ ] Resource-pack texture generator produces valid PNG files.
- [ ] Datapack and resource-pack ZIPs contain pack.mcmeta at archive root.

## Cow interaction
- [ ] Adult Cow first right-click freezes it without sitting/taming.
- [ ] Frozen Cow remains at its anchor when pushed or hit by knockback.
- [ ] Second right-click starts only one opening animation.
- [ ] Upper body rotates around the centre seam; lower body does not move.
- [ ] The real Cow model is not visible underneath the display parts.
- [ ] Inventory appears after the open animation.
- [ ] Close after taking every item: lid closes, display parts disappear, Cow returns to normal AI.
- [ ] Close after taking some items: same cleanup and restore behavior.
- [ ] Close without taking any items: same cleanup and restore behavior; chest cannot reopen.
- [ ] A later right-click is handled by normal Cow interaction, not another chest.
- [ ] Later Cow death gives ordinary vanilla drops and XP.

## Failure and cleanup paths
- [ ] Player disconnects while the lid is opening.
- [ ] Player disconnects while the inventory is open.
- [ ] Cow dies before the chest opens.
- [ ] Cow is removed during the animation.
- [ ] Chunk unload during freeze.
- [ ] Chunk unload while opening/inventory is open.
- [ ] Chunk reload restores a frozen Cow or safely consumes an interrupted opening.
- [ ] Plugin disable/reload removes temporary display entities.
- [ ] Server restart does not leave a hidden Cow or orphaned display part.
- [ ] Two players click the same Cow at nearly the same time; only one inventory opens.

## Visual QA
- [ ] Correct orientation from all sides.
- [ ] Body scale matches the Cow hitbox.
- [ ] No gap or overlap at the middle seam.
- [ ] No clipping between the top and bottom model parts while rotating.
- [ ] Model closes exactly to its original pose.
- [ ] Resource pack is loaded by every test client.

## Later full-gameplay modules
- [ ] Every vanilla mob has a loot table or documented inheritance mapping.
- [ ] Item count and stack sizes vary.
- [ ] Common rewards occur more often than rare rewards.
- [ ] Custom equipment is rare; no full armor set per chest.
- [ ] Boss pools are stronger but do not guarantee a specific item.
- [ ] Loot persists through relog, chunk unload and restart.
