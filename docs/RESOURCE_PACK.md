# Liquid Gold Resource Pack

The companion resource pack is required only for the visual distinction between Liquid Gold and ordinary Water/Lava. Gameplay remains in the Paper plugin.

## Target

- Minecraft Java 26.3
- Resource Pack format: 97.1
- No client gameplay mod or Fabric API required
- No datapack required

## Included assets

- A pale yellow/golden Liquid Gold Bucket model and texture.
- Eight translucent gold flow-level models.
- A softly animated gold liquid texture.
- The pack does not replace the normal Lava Bucket, Lava texture, Water texture or normal Water bucket.

## Install

1. Download MobChests-Liquid-Gold-ResourcePack.zip from the same GitHub Actions artifact as the plugin JAR.
2. Copy the ZIP directly into .minecraft/resourcepacks/.
3. Do not unzip it and do not put it inside another ZIP/folder.
4. Enable it in Options → Resource Packs.
5. Press F3+T only after enabling/updating the pack if the client needs a reload.

For a server-wide pack, host the ZIP and configure Paper/Minecraft's normal server resource-pack URL and SHA-1. This repository does not host that ZIP automatically.

## Technical note

The service uses real vanilla WATER block states underneath to provide swimming and collision. It creates an ItemDisplay overlay with these custom model IDs:
- mobchests:liquid_gold_bucket
- mobchests:liquid_gold_flow_0 through mobchests:liquid_gold_flow_7

The plugin owns flow, source creation, bucket fill/pour, drinking and status effects. The pack only supplies colour/model rendering.
