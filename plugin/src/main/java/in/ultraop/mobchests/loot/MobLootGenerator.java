package in.ultraop.mobchests.loot;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mob-family and dimension-aware loot generator used by configured vanilla Mobs.
 */
public final class MobLootGenerator {
    private static final int INVENTORY_SIZE = 54;
    private static final List<String> GENERIC_COMMON = List.of(
            "IRON_NUGGET", "COAL", "BREAD", "REDSTONE", "LAPIS_LAZULI", "COPPER_INGOT");
    private static final List<String> PASSIVE_COMMON = List.of(
            "WHEAT", "APPLE", "CARROT", "POTATO", "BREAD", "LEATHER");
    private static final List<String> UNDEAD_COMMON = List.of(
            "ROTTEN_FLESH", "BONE", "ARROW", "IRON_NUGGET", "COAL");
    private static final List<String> SKELETON_COMMON = List.of(
            "BONE", "ARROW", "BOW", "STRING", "COAL");
    private static final List<String> NETHER_COMMON = List.of(
            "GOLD_NUGGET", "QUARTZ", "GLOWSTONE_DUST", "NETHER_BRICK", "MAGMA_CREAM");
    private static final List<String> AQUATIC_COMMON = List.of(
            "COD", "SALMON", "PRISMARINE_SHARD", "PRISMARINE_CRYSTALS", "KELP");

    private MobLootGenerator() { }

    public static Inventory create(java.util.UUID mobId, EntityType type, World.Environment environment,
                                   UltraOPMobChestsPlugin plugin) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        MobChestInventoryHolder holder = new MobChestInventoryHolder(mobId);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE,
                Component.text(displayName(type.name()) + " Treasure", NamedTextColor.GOLD));
        holder.bind(inventory);

        int configuredMinimum = plugin.getConfig().getInt("loot.minimum-item-stacks", 0);
        // Treat old pre-reduction configs (e.g. 14 minimum stacks) as the new zero minimum.
        int minStacks = configuredMinimum > 4 ? 0 : Math.max(0, configuredMinimum);
        int maxStacks = clamp(plugin.getConfig().getInt("loot.maximum-item-stacks", 4), minStacks, 4);
        int stacks = random.nextInt(minStacks, maxStacks + 1);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) slots.add(slot);
        Collections.shuffle(slots, random);

        for (int i = 0; i < stacks; i++) {
            inventory.setItem(slots.get(i), reward(type, environment, rollRarity(type, plugin, random), plugin, random));
        }

        int nextSpecialSlot = stacks;
        double gearChance = switch (tier(type)) {
            case COMMON -> chance(plugin, "loot.tier-equipment-chance.common", 0.01);
            case MID -> chance(plugin, "loot.tier-equipment-chance.mid", 0.01);
            case STRONG -> chance(plugin, "loot.tier-equipment-chance.strong", 0.03);
            case BOSS -> chance(plugin, "loot.tier-equipment-chance.boss", 0.05);
        };
        // Bat Elytra chance is independent of the normal roll count, replacing one slot if needed.
        if (type == EntityType.BAT
                && random.nextDouble() < chance(plugin, "loot.bat-elytra-chance", 0.02)) {
            inventory.setItem(slots.get(0), new ItemStack(Material.ELYTRA, 1));
        } else if (nextSpecialSlot < Math.min(4, slots.size())
                && random.nextDouble() < gearChance) {
            // Special gear occupies one of the four allowed item slots, never a fifth.
            inventory.setItem(slots.get(nextSpecialSlot), tierEquipment(type, random));
        }
        return inventory;
    }

    private static Rarity rollRarity(EntityType type, UltraOPMobChestsPlugin plugin,
                                     ThreadLocalRandom random) {
        double common = weight(plugin, "common", 80.0);
        double uncommon = weight(plugin, "uncommon", 17.0);
        double rare = weight(plugin, "rare", 2.5);
        double epic = weight(plugin, "epic", 0.4);
        double legendary = weight(plugin, "legendary", 0.09);
        double mythic = weight(plugin, "mythic", 0.01);
        double total = common + uncommon + rare + epic + legendary + mythic;
        if (total <= 0.0) return Rarity.COMMON;

        // Boss mobs receive a small rarity bias, not a guaranteed named reward.
        double roll = random.nextDouble(total);
        if (isBoss(type)) roll = Math.min(Math.nextDown(total), roll + total * 0.06);
        if (roll < common) return Rarity.COMMON;
        roll -= common;
        if (roll < uncommon) return Rarity.UNCOMMON;
        roll -= uncommon;
        if (roll < rare) return Rarity.RARE;
        roll -= rare;
        if (roll < epic) return Rarity.EPIC;
        roll -= epic;
        if (roll < legendary) return Rarity.LEGENDARY;
        return Rarity.MYTHIC;
    }

    private static double weight(UltraOPMobChestsPlugin plugin, String key, double fallback) {
        return Math.max(0.0, plugin.getConfig().getDouble("rarity." + key, fallback));
    }

    private static double chance(UltraOPMobChestsPlugin plugin, String path, double fallback) {
        return Math.max(0.0, Math.min(1.0, plugin.getConfig().getDouble(path, fallback)));
    }

    private static ItemStack reward(EntityType type, World.Environment environment, Rarity rarity,
                                    UltraOPMobChestsPlugin plugin,
                                    ThreadLocalRandom random) {
        return switch (rarity) {
            case COMMON -> createLootStack(randomMaterial(commonPool(type, environment)), random, 1, 4);
            case UNCOMMON -> createLootStack(randomMaterial(uncommonPool(type, environment)), random, 1, 2);
            case RARE -> random.nextDouble() < 0.02
                    ? new ItemStack(randomMaterial(ARMOR_TRIM_ITEMS), 1)
                    : createLootStack(randomMaterial(List.of("DIAMOND", "EMERALD", "GOLDEN_APPLE",
                    "ENDER_EYE", "AMETHYST_SHARD", "EXPERIENCE_BOTTLE")), random, 1, 1);
            case EPIC -> vanillaEnchantedBook(random);
            case LEGENDARY, MYTHIC -> createLootStack(randomMaterial(List.of(
                    "DIAMOND", "EMERALD", "GOLDEN_APPLE", "TOTEM_OF_UNDYING")), random, 1, 1);
        };
    }

    private static List<String> familyCommonPool(EntityType type) {
        String name = type.name();
        if (name.equals("COW")) return List.of("BEEF", "LEATHER", "WHEAT", "BREAD", "APPLE");
        if (name.equals("PIG") || name.equals("HOGLIN") || name.equals("ZOGLIN")) {
            return List.of("PORKCHOP", "CARROT", "POTATO", "LEATHER", "GOLD_NUGGET");
        }
        if (name.equals("SHEEP")) return List.of("MUTTON", "WHITE_WOOL", "STRING", "WHEAT");
        if (name.equals("CHICKEN")) return List.of("CHICKEN", "FEATHER", "EGG", "WHEAT_SEEDS");
        if (name.contains("SKELETON") || name.equals("BOGGED")) return SKELETON_COMMON;
        if (name.equals("ZOMBIE") || name.equals("DROWNED") || name.equals("HUSK")
                || name.equals("ZOMBIFIED_PIGLIN")) return UNDEAD_COMMON;
        if (name.equals("SPIDER") || name.equals("CAVE_SPIDER")) {
            return List.of("STRING", "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "BROWN_MUSHROOM");
        }
        if (name.equals("CREEPER")) return List.of("GUNPOWDER", "TNT", "COAL", "REDSTONE");
        if (name.equals("ENDERMAN") || name.equals("ENDERMITE") || name.equals("SHULKER")) {
            return List.of("ENDER_PEARL", "CHORUS_FRUIT", "PURPUR_BLOCK", "OBSIDIAN");
        }
        if (name.equals("BLAZE") || name.equals("GHAST") || name.equals("MAGMA_CUBE")
                || name.equals("PIGLIN") || name.equals("PIGLIN_BRUTE")) return NETHER_COMMON;
        if (name.contains("GUARDIAN") || name.equals("DOLPHIN") || name.equals("SQUID")
                || name.equals("GLOW_SQUID") || name.equals("TURTLE") || name.equals("COD")
                || name.equals("SALMON") || name.equals("PUFFERFISH") || name.equals("TROPICAL_FISH")) {
            return AQUATIC_COMMON;
        }
        if (name.equals("WARDEN")) return List.of("SCULK_CATALYST", "ECHO_SHARD", "SCULK", "AMETHYST_SHARD");
        if (name.equals("ENDER_DRAGON") || name.equals("WITHER")) {
            return List.of("DIAMOND", "GOLD_INGOT", "EXPERIENCE_BOTTLE", "ENDER_PEARL");
        }
        if (isPassive(name)) return PASSIVE_COMMON;
        return GENERIC_COMMON;
    }

    private static List<String> commonPool(EntityType type, World.Environment environment) {
        // Keep common drops practical and compact; avoid flooding players with
        // decorative blocks, dye piles and low-value starter tools.
        List<String> pool = new ArrayList<>(familyCommonPool(type));
        pool.addAll(List.of(
                "COAL", "IRON_NUGGET", "COPPER_INGOT", "REDSTONE", "LAPIS_LAZULI",
                "ARROW", "STRING", "STICK", "TORCH", "BREAD", "APPLE",
                "CARROT", "POTATO", "WHEAT", "LEATHER"));
        pool.addAll(dimensionCommonPool(environment));
        return pool;
    }

    private static List<String> dimensionCommonPool(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> List.of(
                    "OBSIDIAN", "FLINT", "FLINT_AND_STEEL", "IRON_INGOT",
                    "GOLD_INGOT", "STONE", "DIRT", "GRAVEL", "SAND", "CLAY_BALL",
                    "DEEPSLATE", "COBBLED_DEEPSLATE", "TUFF", "CALCITE",
                    "MOSS_BLOCK", "LANTERN", "CHAIN", "COPPER_INGOT");
            case NETHER -> List.of(
                    "NETHER_QUARTZ_ORE", "QUARTZ", "GLOWSTONE_DUST", "MAGMA_CREAM",
                    "BLAZE_ROD", "GOLD_NUGGET", "GOLD_INGOT", "NETHER_BRICK",
                    "SOUL_SAND", "SOUL_SOIL", "NETHERRACK", "BASALT",
                    "POLISHED_BASALT", "BLACKSTONE", "GILDED_BLACKSTONE",
                    "CRIMSON_STEM", "WARPED_STEM", "SHROOMLIGHT");
            case THE_END -> List.of(
                    "ENDER_PEARL", "CHORUS_FRUIT", "PURPUR_BLOCK", "END_STONE",
                    "PHANTOM_MEMBRANE", "AMETHYST_SHARD", "POPPED_CHORUS_FRUIT",
                    "PURPUR_PILLAR", "END_ROD", "SHULKER_SHELL");
            default -> List.of("PAPER", "COMPASS", "CLOCK", "BOOK");
        };
    }

    private static List<String> uncommonPool(EntityType type, World.Environment environment) {
        List<String> pool = new ArrayList<>(List.of(
                "IRON_INGOT", "GOLD_INGOT", "EMERALD", "EXPERIENCE_BOTTLE",
                "GOLDEN_CARROT", "ENDER_PEARL", "ENCHANTED_BOOK", "OBSIDIAN",
                "POTION", "SPLASH_POTION", "NAME_TAG", "SADDLE", "LEAD",
                "SLIME_BALL", "IRON_PICKAXE", "IRON_AXE", "IRON_SHOVEL",
                "CROSSBOW", "SPYGLASS", "GOAT_HORN", "TRIDENT", "MAGMA_CREAM"));
        if (isExplorer(type.name())) pool.add("MAP");
        pool.addAll(dimensionCommonPool(environment));
        return pool;
    }

    private static boolean isExplorer(String typeName) {
        return List.of("VILLAGER", "WANDERING_TRADER", "TRADER_LLAMA",
                "DONKEY", "MULE", "LLAMA").contains(typeName);
    }

    private static final List<String> ARMOR_TRIM_ITEMS = List.of(
            "SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", "DUNE_ARMOR_TRIM_SMITHING_TEMPLATE",
            "COAST_ARMOR_TRIM_SMITHING_TEMPLATE", "WILD_ARMOR_TRIM_SMITHING_TEMPLATE",
            "WARD_ARMOR_TRIM_SMITHING_TEMPLATE", "EYE_ARMOR_TRIM_SMITHING_TEMPLATE",
            "VEX_ARMOR_TRIM_SMITHING_TEMPLATE", "TIDE_ARMOR_TRIM_SMITHING_TEMPLATE",
            "SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", "RIB_ARMOR_TRIM_SMITHING_TEMPLATE",
            "SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE", "WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE",
            "SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", "SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE",
            "RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", "HOST_ARMOR_TRIM_SMITHING_TEMPLATE",
            "FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", "BOLT_ARMOR_TRIM_SMITHING_TEMPLATE");

    private static List<String> dimensionRarePool(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> List.of(
                    "OBSIDIAN", "FLINT_AND_STEEL", "DIAMOND", "EMERALD",
                    "ENDER_PEARL", "GOLDEN_APPLE", "IRON_BLOCK");
            case NETHER -> List.of(
                    "ANCIENT_DEBRIS", "NETHERITE_SCRAP", "BLAZE_ROD",
                    "NETHERITE_INGOT", "GOLD_BLOCK", "MAGMA_CREAM");
            case THE_END -> List.of(
                    "SHULKER_SHELL", "DRAGON_BREATH", "CHORUS_FRUIT",
                    "ENDER_EYE", "END_CRYSTAL");
            default -> List.of(
                    "DIAMOND", "EMERALD", "GOLDEN_APPLE", "ENDER_EYE");
        };
    }

    private static boolean isPassive(String name) {
        return List.of("ALLAY", "ARMADILLO", "AXOLOTL", "BAT", "BEE", "CAMEL", "CAT",
                "DONKEY", "FOX", "FROG", "GOAT", "HORSE", "LLAMA", "MOOSHROOM",
                "MULE", "OCELOT", "PANDA", "PARROT", "RABBIT", "SNIFFER", "STRIDER",
                "TRADER_LLAMA", "VILLAGER", "WANDERING_TRADER", "WOLF").contains(name);
    }

    private static boolean isBoss(EntityType type) {
        return switch (type.name()) {
            case "ENDER_DRAGON", "WITHER", "WARDEN", "ELDER_GUARDIAN" -> true;
            default -> false;
        };
    }

    private static MobTier tier(EntityType type) {
        String name = type.name();
        if (isBoss(type) || name.equals("WARDEN")) return MobTier.BOSS;
        if (List.of("RAVAGER", "EVOKER", "VINDICATOR", "PIGLIN_BRUTE", "BLAZE",
                "GHAST", "SHULKER", "HOGLIN", "ZOGLIN").contains(name)) return MobTier.STRONG;
        if (List.of("ZOMBIE", "HUSK", "DROWNED", "SKELETON", "STRAY", "CREEPER",
                "SPIDER", "CAVE_SPIDER", "ENDERMAN", "WITCH", "PHANTOM", "PILLAGER",
                "GUARDIAN", "PIGLIN", "BREEZE", "SLIME", "MAGMA_CUBE", "VEX",
                "SILVERFISH", "ENDERMITE").contains(name)) return MobTier.MID;
        return MobTier.COMMON;
    }

    private static ItemStack tierEquipment(EntityType type, ThreadLocalRandom random) {
        String name = type.name();
        boolean boss = isBoss(type) || name.equals("WARDEN");
        boolean strong = boss || List.of("RAVAGER", "EVOKER", "VINDICATOR", "PIGLIN_BRUTE",
                "BLAZE", "GHAST", "SHULKER", "HOGLIN", "ZOGLIN").contains(name);
        boolean mid = strong || List.of("ZOMBIE", "HUSK", "DROWNED", "SKELETON", "STRAY",
                "CREEPER", "SPIDER", "CAVE_SPIDER", "ENDERMAN", "WITCH", "PHANTOM",
                "PILLAGER", "GUARDIAN", "PIGLIN", "BREEZE", "SLIME", "MAGMA_CUBE").contains(name);
        Material[] common = {Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
                Material.IRON_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.IRON_SWORD, Material.GOLDEN_SWORD};
        Material[] diamond = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD,
                Material.DIAMOND_AXE, Material.BOW, Material.CROSSBOW};
        Material[] netherite = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD,
                Material.NETHERITE_AXE, Material.TRIDENT, Material.CROSSBOW};
        Material selected = boss ? netherite[random.nextInt(netherite.length)]
                : mid ? diamond[random.nextInt(diamond.length)] : common[random.nextInt(common.length)];
        ItemStack item = new ItemStack(selected);
        ItemMeta meta = item.getItemMeta();
        String[] keys = selected.name().endsWith("_HELMET") || selected.name().endsWith("_CHESTPLATE")
                || selected.name().endsWith("_LEGGINGS") || selected.name().endsWith("_BOOTS")
                ? new String[]{"protection", "unbreaking", "thorns", "feather_falling"}
                : selected == Material.BOW ? new String[]{"power", "flame", "punch", "unbreaking"}
                : selected == Material.CROSSBOW ? new String[]{"quick_charge", "piercing", "multishot", "unbreaking"}
                : selected == Material.TRIDENT ? new String[]{"impaling", "loyalty", "riptide", "unbreaking"}
                : new String[]{"sharpness", "smite", "fire_aspect", "looting", "unbreaking", "mending"};
        int count = boss ? random.nextInt(2, 4) : strong ? random.nextInt(1, 3) : 1;
        int max = boss ? 8 : strong ? 5 : mid ? 3 : 2;
        for (int i = 0; i < count; i++) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(keys[random.nextInt(keys.length)]));
            if (enchantment != null) meta.addEnchant(enchantment, random.nextInt(1, max + 1), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack enchantedGear(ThreadLocalRandom random) {
        Material[] choices = {Material.DIAMOND_SWORD, Material.BOW, Material.IRON_CHESTPLATE,
                Material.DIAMOND_PICKAXE, Material.CROSSBOW};
        String[][] enchantmentKeys = {
                {"sharpness", "unbreaking"}, {"power", "unbreaking"},
                {"protection", "unbreaking"}, {"efficiency", "unbreaking"},
                {"quick_charge", "unbreaking"}
        };
        int selected = random.nextInt(choices.length);
        ItemStack item = new ItemStack(choices[selected]);
        ItemMeta meta = item.getItemMeta();
        String[] keys = enchantmentKeys[selected];
        Enchantment enchantment = Registry.ENCHANTMENT.get(
                NamespacedKey.minecraft(keys[random.nextInt(keys.length)]));
        if (enchantment != null) meta.addEnchant(enchantment, random.nextInt(1, 5), true);
        item.setItemMeta(meta);
        return item;
    }




    /**
     * This is a plugin-applied enchanted book, not a vanilla anvil book.
     * Keeping the high level in PDC rather than EnchantmentStorageMeta avoids
     * Minecraft's untranslated enchantment.level.N tooltip for levels > max.
     */
    private static ItemStack vanillaEnchantedBook(ThreadLocalRandom random) {
        String[] keys = {"sharpness", "protection", "efficiency", "unbreaking", "fortune",
                "looting", "feather_falling", "respiration", "power", "flame", "mending",
                "silk_touch", "depth_strider", "soul_speed", "swift_sneak"};
        Enchantment enchantment = null;
        for (int attempt = 0; attempt < keys.length && enchantment == null; attempt++) {
            String key = keys[random.nextInt(keys.length)];
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
        }
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (enchantment != null) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(enchantment,
                    random.nextInt(1, Math.max(1, enchantment.getMaxLevel()) + 1), true);
            book.setItemMeta(meta);
        }
        return book;
    }

    private static ItemStack createLootStack(Material material, ThreadLocalRandom random,
                                              int minimum, int maximum) {
        if (material == Material.POTION || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION) return randomPotion(material, random);
        if (material == Material.ENCHANTED_BOOK) return vanillaEnchantedBook(random);
        return stack(material, random, minimum, maximum);
    }

    private static ItemStack randomPotion(Material material, ThreadLocalRandom random) {
        PotionType[] options = {
                PotionType.SWIFTNESS, PotionType.STRENGTH, PotionType.HEALING,
                PotionType.REGENERATION, PotionType.FIRE_RESISTANCE, PotionType.NIGHT_VISION,
                PotionType.WATER_BREATHING, PotionType.SLOW_FALLING, PotionType.LEAPING,
                PotionType.LONG_SWIFTNESS, PotionType.LONG_STRENGTH,
                PotionType.LONG_FIRE_RESISTANCE, PotionType.LONG_WATER_BREATHING,
                PotionType.STRONG_HEALING, PotionType.STRONG_STRENGTH
        };
        ItemStack potion = new ItemStack(material);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(options[random.nextInt(options.length)]);
        potion.setItemMeta(meta);
        return potion;
    }


    private static ItemStack stack(Material material, ThreadLocalRandom random, int min, int max) {
        if (material == null || material.isAir()) material = Material.BREAD;
        int maxAllowed = Math.max(1, material.getMaxStackSize());
        // Navigation tools and trim templates are single rare finds, not piles.
        if (material == Material.COMPASS || material == Material.RECOVERY_COMPASS
                || material == Material.MAP || material.name().endsWith("_ARMOR_TRIM_SMITHING_TEMPLATE")) {
            return new ItemStack(material, 1);
        }
        int lower = Math.max(1, Math.min(min, maxAllowed));
        int upper = Math.max(lower, Math.min(max, maxAllowed));
        return new ItemStack(material, random.nextInt(lower, upper + 1));
    }

    private static Component itemTitle(String text, NamedTextColor color) {
        return Component.text(text, color)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .decoration(TextDecoration.BOLD, TextDecoration.State.TRUE);
    }

    private static Component itemLore(String text, NamedTextColor color) {
        return Component.text(text, color)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE);
    }

    private static Material randomMaterial(List<String> names) {
        List<Material> valid = new ArrayList<>();
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isItem() || material.isAir()
                    || material.name().endsWith("_SPAWN_EGG")
                    || isRestrictedLootItem(material)) {
                continue;
            }
            valid.add(material);
        }
        return valid.isEmpty() ? Material.BREAD
                : valid.get(ThreadLocalRandom.current().nextInt(valid.size()));
    }

    private static boolean isRestrictedLootItem(Material material) {
        return switch (material) {
            case AIR, BEDROCK, BARRIER, COMMAND_BLOCK, CHAIN_COMMAND_BLOCK,
                    REPEATING_COMMAND_BLOCK, STRUCTURE_BLOCK, STRUCTURE_VOID,
                    JIGSAW, DEBUG_STICK, KNOWLEDGE_BOOK, LIGHT, SPAWNER,
                    END_PORTAL_FRAME, COMMAND_BLOCK_MINECART -> true;
            default -> false;
        };
    }

    private static String displayName(String name) { return title(name.replace('_', ' ')); }

    private static String title(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum MobTier { COMMON, MID, STRONG, BOSS }
    private enum Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC }
}
