# Liquid Gold Resource Pack

Minecraft Java 26.3 resource pack (format 97.1).

## Visuals

- Starts with the original Minecraft 26.3 vanilla inventory PNGs for potato, baked potato, cooked meats, bread, carrot, fish, pumpkin pie and lava bucket.
- Applies a restrained pale-gold tint (18% blend) to the original food PNGs. Original pixel art, outline, transparency, dimensions and silhouette are preserved.
- Uses the vanilla Lava Bucket inventory sprite as the Liquid Gold container. Only the lava inside the bucket is recoloured to pale gold; the metal bucket shape is unchanged.
- Provides eight animated golden fluid-level models. The overlay is softly translucent and uses full-block geometry to minimise blue water pixels appearing through its seams.
- No vanilla water, lava, food or bucket textures are replaced globally.

## Gameplay

The Paper plugin places real vanilla WATER blocks underneath the gold visual. Minecraft therefore controls spread speed, downhill flow, source-block creation and infinite-source refill. The plugin only tracks the Liquid Gold cells, draws the gold overlay and converts an empty bucket pickup into a Liquid Gold Bucket.

Golden Food items use vanilla edible materials and retain their original food consumption animation. Each food also grants its own randomly selected bonus for 5–10 seconds. They can be eaten even when the hunger bar is full, making them easier to test.

## Install

1. Copy this ZIP to the client's `.minecraft/resourcepacks/` folder.
2. Enable **MobChests Liquid Gold** in Options → Resource Packs.
3. Install the companion Paper plugin JAR on the server.
4. OPs can use `/mobchests items` to open the custom-items catalogue.

No client gameplay mod or datapack is required.

## Texture sources and credits

The vanilla sprite PNGs are taken from the Minecraft 26.3 texture manifest/assets in the `minecraft-textures` package (version 26.3.0), maintained by **destruc7i0n**. The package asks users to credit TheDestruc7i0n when reusing the assets. Minecraft item art remains the property of Mojang Studios; this pack is not affiliated with Mojang.

Minecraft Wiki gameplay reference pages used for fluid and food behaviour:
- https://minecraft.wiki/w/Water
- https://minecraft.wiki/w/Food_mechanics

The Wiki documents Java water flowing 7 blocks horizontally at 5 game ticks per block and the vanilla source-block regeneration rule. Food mechanic documentation describes the normal 32-tick eat duration.
