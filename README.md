# MobChests

Paper plugin for Minecraft Java 26.3 (Java 25).

## Features
- Mob entities freeze, animate, and open as reusable treasure chests through the existing MobChests interaction flow.
- Chest contents persist when the inventory closes and across chunk unload/reload; unused loot drops when the mob dies after its chest has been opened.
- Loot scales by mob strength, with randomized vanilla rewards and rare high-tier equipment.
- Five mob-themed weapons can appear as rare chest rewards:
  - Iron Golem — **Mjolnier** (Mace)
  - Warden — **Kamish Wrath Alpha** (Netherite Sword)
  - Wither — **Demonic Bow**
  - Elder Guardian — **Aquatic Rage** (Diamond Spear)
  - Ender Dragon — **Blades of War** (Crossbow)
- Use `/mobchest items` to browse the weapons. Administrators can use `/mobchest give <player> <item>`.
- The companion Blades of War resource pack supplies the custom weapon models and textures.

## Commands
- `/mobchests info`
- `/mobchests help`
- `/mobchests status`
- `/mobchests items`
- `/mobchests reload` (admin)
- `/mobchests give <player> <item>` (admin)

## Build
GitHub Actions builds the Paper plugin with Java 25 and Gradle 9.5.1. The JAR is created in `plugin/build/libs/`.
