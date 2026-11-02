# Testing Checklist

## Build
- [ ] Java 25 toolchain resolves.
- [ ] Paper 26.3 API resolves.
- [ ] Gradle clean build passes.
- [ ] JAR contains plugin.yml and default configs.

## Commands
- [ ] info is the single version/build info panel.
- [ ] help lists command meanings and permissions.
- [ ] reload rejects non-admins.
- [ ] status accurately reports implemented modules.

## Interaction
- [ ] First right-click freezes.
- [ ] Second right-click opens exactly once.
- [ ] No sit/tame behavior.
- [ ] Body splits at middle; lid opens; base stays fixed.
- [ ] No duplicate session for two players.
- [ ] GUI close discards remainder; no reroll.
- [ ] Mob returns to normal AI.
- [ ] Later death yields vanilla drops and XP, with no extra OP loot.

## Loot
- [ ] Every mob has table or explicit mapping.
- [ ] Item count and stack sizes vary.
- [ ] Common rewards more frequent than rare.
- [ ] Custom equipment rare; no full set per chest.
- [ ] Boss pools stronger but not guaranteed.
- [ ] Results persist through relog, chunk unload and restart.

## Visuals
- [ ] Pack loads.
- [ ] Cow model opens correctly.
- [ ] All viewers see same animation.
- [ ] Different mob sizes/poses align.
- [ ] Display entities cleaned on death/unload.
