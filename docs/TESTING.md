# Testing Checklist

## Automated build and static checks
- [ ] Java 25 and Paper 26.3 API resolve.
- [ ] Gradle clean build succeeds.
- [ ] JAR contains MobChestListener, MobLootGenerator, CustomMobItems, and MobChestsCommand.
- [ ] JAR excludes retired Liquid Gold and Golden Food classes.
- [ ] Every `@EventHandler` method declares exactly one Bukkit/Paper Event parameter.
- [ ] Plugin enables without listener-registration exceptions.

## Mob chest lifecycle
- [ ] First right-click freezes a supported mob; second right-click opens its chest.
- [ ] Opening and closing head animation, sounds, and particles run without task leaks.
- [ ] Mob returns to its original AI, awareness, gravity, visibility, and collision state after closing.
- [ ] Chest contents and slot positions persist after closing and reopening.
- [ ] Remaining loot persists through chunk unload/reload and plugin shutdown/restart.
- [ ] Player quit, mob death, and chunk unload during an open session do not duplicate or lose saved items.
- [ ] Used chest loot replaces vanilla item drops while vanilla XP remains.

## Loot and custom weapons
- [ ] Common, mid-tier, strong, and boss mobs receive randomized loot.
- [ ] Bosses have a chance, not a guarantee, of Netherite-tier rewards.
- [ ] Five mob-specific weapons can appear at their configured rare rates.
- [ ] Relic insertion never overwrites normal chest rewards.
- [ ] Warden guaranteed equipment and OP-enchantment limits behave as intended.
- [ ] Inventory GUI clicks and drags cannot move display items; receiving items with a full inventory drops overflow safely.
- [ ] Admin give command and multiword tab completion work.

## Commands and permissions
- [ ] info/help/status/items work for permitted players.
- [ ] reload and give are restricted to admins.
- [ ] Console use of items gives a clear player-only message.
