# MobChests

Paper plugin for Minecraft Java 26.3 (Java 25).

## Features
- Mob entities freeze, animate, and open as treasure chests through the existing MobChests interaction flow.
- Existing chest opening/closing animation, sounds, particles, persistence, and recovery remain enabled.
- Loot scales with mob strength: common mobs receive basic rewards, mid-tier mobs can receive diamond gear, strong mobs receive stronger enchanted gear, and bosses/Warden have a rare chance at Netherite-tier vanilla rewards.
- Loot is randomized; no mob is guaranteed an overpowered reward.
- All rewards use vanilla Minecraft items and enchantments.

## Removed
All custom items and their systems have been removed: Liquid Gold, Golden Food, named custom equipment, custom relics, custom enchantment books, item catalogue and give/list commands, and the entire custom resource pack including its textures and models.

## Commands
- `/mobchests info`
- `/mobchests help`
- `/mobchests status`
- `/mobchests reload` (admin)

## Build
GitHub Actions builds the Paper plugin with Java 25 and Gradle 9.5.1. The JAR is created in `plugin/build/libs/`.
