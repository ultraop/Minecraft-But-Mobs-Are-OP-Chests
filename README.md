# Mob Chests — Mobs Are OP Chests

**Paper 26.3 plugin | Java 25 | No client mod, resource pack or datapack required**

## Current mob animation

The old ItemDisplay/resource-pack model path is retired. The original vanilla Mob stays visible and keeps its normal dimensions. During the chest animation the plugin disables AI and applies a controlled entity pitch with `Entity.setRotation(yaw, pitch)`, while holding the body yaw at its saved value. This prevents the Mob's normal look goals from fighting the pose and repeatedly turning the head up and down.

- First right-click: freezes the Mob in place with a subtle pitch tremble.
- Second right-click: smoothly raises the vanilla pitch to approximately -88 degrees.
- While the GUI is open: the open pitch is held steady.
- Closing the GUI: pitch, AI, awareness, gravity, collision and the saved rotation are restored.
- The Mob keeps its original width, height, hitbox and vanilla texture.

This is a vanilla pitch pose, not a true split-body chest hinge. Different Mob renderers may interpret pitch differently; Paper's public API does not expose independent rotation for every head, abdomen, leg or squid tentacle.

## Chest and loot behaviour

- One chest session per Mob. Closing it after taking all, some or none of the contents permanently consumes that chest.
- Each chest generates 14–18 base occupied stacks, plus any separate special rewards. Stack quantities vary.
- Half of the occupied stacks (rounded up) are removed from the GUI and launched outward/upward. Horizontal velocity is intentionally low to keep the items within roughly 3–4 blocks of the Mob.
- The other half stays in the GUI. Ejected items are removed before spawning, so they cannot be duplicated.
- On a used Mob's death, normal vanilla item drops are replaced by the remaining saved chest contents. Normal vanilla XP remains. If the chest was never opened, normal vanilla drops and XP are preserved.

## Loot variety

Common pools now mix family-specific loot with broad survival items:

- Blocks and stone: stone, dirt, gravel, sand, granite, diorite, andesite, tuff, calcite, deepslate, cobbled deepslate, moss, mud, glass and dimension-themed blocks.
- Materials: bones, bone meal, ink sacs, glow ink sacs, all dye colours, feathers, string, leather, metals, coal, redstone, lapis, seeds and crops.
- Utility/decor: paper, clocks, books, chains, lanterns, candles, barrels, chests, name tags, leads, saddles and item frames.
- Signed written books with short explorer notes.
- Potions, splash potions and lingering potions from a safe set of vanilla potion types.
- Normal vanilla Enchanted Books with valid vanilla levels; these remain usable in an anvil.
- Smithing templates and armour trims appear in uncommon pools.
- Maps are limited to explorer/trader-type Mob pools, not added to every Mob.

Dimension progression remains separate:
- Overworld: obsidian, flint and steel, iron and building/navigation supplies.
- Nether: quartz, blaze rods, gold and rare ancient debris/Netherite scrap/ingot.
- End: End-related and Shulker progression supplies.

Netherite progression rewards are only selected in the Nether pool. Creative/admin-only items such as command blocks, barrier, bedrock, debug stick, knowledge book, spawners and spawn eggs are filtered out of random loot.

## Custom Enchantment Books

Custom Ancient Enchanted Books use a plugin-applied enchantment ID and a bounded level. They do not store unsafe level 30–60 enchantments in Vanilla EnchantmentStorageMeta; this prevents the client tooltip from showing strings such as `enchantment.level.46`.

**How to apply:** hold the Ancient Enchanted Book in the main hand, put the compatible item in the off-hand, then right-click. The plugin cancels the off-hand's normal equip/use action while the book is being used, checks compatibility and conflicts, applies the enchantment and consumes the book only on success.

Custom ranges are intentionally stronger than vanilla but bounded:

| Enchantment | Custom level range | Compatible gear |
|---|---:|---|
| Sharpness, Smite, Bane of Arthropods | 8–12 | Sword / compatible weapon |
| Power | 8–12 | Bow |
| Protection | 6–8 | Armour |
| Blast, Fire, Projectile Protection | 5–7 | Armour |
| Efficiency | 8–10 | Tools |
| Unbreaking | 6–9 | Compatible gear |
| Fortune, Looting | 4–6 | Compatible tool / sword |
| Feather Falling | 6–8 | Boots |
| Respiration | 4–6 | Helmet |
| Sweeping Edge | 4–6 | Sword |
| Fire Aspect | 2–4 | Sword |
| Knockback | 2–4 | Sword |
| Depth Strider, Soul Speed | 4–5 | Boots |
| Swift Sneak | 4–5 | Leggings |
| Thorns | 4–5 | Armour |
| Quick Charge, Piercing | 3–6 | Crossbow |
| Impaling, Loyalty, Riptide | 3–8 | Trident |

The compatibility check means, for example, Respiration will only apply to a helmet and Feather Falling only to boots. Conflicting enchantments are rejected. Mending and other non-scaling level-I enchantments remain available as ordinary vanilla Enchanted Books instead of being given meaningless high levels.

## Special rewards and known limits

- Golden Potato, Golden Porkchop and Golden Bread are custom-named vanilla items.
- Emerald Armor is a single green-dyed leather armour piece, not a complete set.
- Liquid Gold Bucket has a 35% test-build chance and a sealed vanilla bucket icon. The actual flowing gold fluid, bucket placement/scooping, infinite-source behaviour and ten-second abilities are not implemented yet; its right-click is blocked so it cannot place ordinary lava.
- Named relics and equipment are loot foundations. Their individual powers, custom enchantment effects beyond the listed vanilla enchantments, Legend points and boss progression are future work.
- The no-resource-pack build cannot provide custom mob or item textures.
- Independent squid tentacle/body-bone animation still requires a client renderer or custom model system.

## Requirements and install

| Requirement | Target |
|---|---|
| Minecraft Java | 26.3 |
| Server | Paper 26.3 |
| Java | 25 |
| Client gameplay mod | Not required |
| Resource pack / datapack | Not required |

1. Stop the Paper server.
2. Replace the old plugin JAR in `plugins/` with the new JAR.
3. Remove any old custom Mob Chests resource pack or datapack from previous builds; this version does not use them.
4. Start Paper 26.3 with Java 25.
5. The new config folder is `plugins/MobChests/`.
6. Use `/mobchests info`, `/mobchests status` and `/mobchests reload`.

## Build

~~~bash
gradle clean build
~~~

## Commands

| Command | Permission | Purpose |
|---|---|---|
| `/mobchests info` | Everyone | Version and creator information |
| `/mobchests help` | Everyone | Command guide |
| `/mobchests status` | Everyone | Runtime and feature status |
| `/mobchests reload` | `mobchests.admin` | Reload configuration |

## Further documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Loot design](docs/LOOT_DESIGN.md)
- [Enchantment books](docs/ENCHANTMENT_BOOKS.md)
- [Testing checklist](docs/TESTING.md)

## License

MIT — see LICENSE.
