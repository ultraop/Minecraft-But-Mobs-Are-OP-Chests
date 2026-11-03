package in.ultraop.mobchests.loot;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;

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

        int minStacks = clamp(plugin.getConfig().getInt("loot.minimum-item-stacks", 14), 1, 36);
        int maxStacks = clamp(plugin.getConfig().getInt("loot.maximum-item-stacks", 18), minStacks, 36);
        int stacks = random.nextInt(minStacks, maxStacks + 1);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) slots.add(slot);
        Collections.shuffle(slots, random);

        for (int i = 0; i < stacks; i++) {
            inventory.setItem(slots.get(i), reward(type, environment, rollRarity(type, plugin, random), plugin, random));
        }

        // Special rewards occupy fresh slots; they never overwrite another reward.
        int nextSpecialSlot = stacks;
        double liquidGoldChance = chance(plugin, "loot.liquid-gold-chance", 0.25);
        if (stacks > 0 && nextSpecialSlot < slots.size() && random.nextDouble() < liquidGoldChance) {
            inventory.setItem(slots.get(nextSpecialSlot++), liquidGoldBucket(plugin));
        }

        double goldenFoodChance = chance(plugin, "loot.golden-food-chance", 0.06);
        if (stacks > 0 && nextSpecialSlot < slots.size() && random.nextDouble() < goldenFoodChance) {
            inventory.setItem(slots.get(nextSpecialSlot++), goldenFood(plugin, random));
        }

        // At most one custom equipment piece per chest, never a full set.
        double gearChance = chance(plugin, "loot.custom-equipment-chance", 0.10);
        if (stacks > 0 && nextSpecialSlot < slots.size() && random.nextDouble() < gearChance) {
            inventory.setItem(slots.get(nextSpecialSlot), customEquipment(plugin, random, environment));
        }
        return inventory;
    }

    private static Rarity rollRarity(EntityType type, UltraOPMobChestsPlugin plugin,
                                     ThreadLocalRandom random) {
        double common = weight(plugin, "common", 70.0);
        double uncommon = weight(plugin, "uncommon", 20.0);
        double rare = weight(plugin, "rare", 7.0);
        double epic = weight(plugin, "epic", 2.0);
        double legendary = weight(plugin, "legendary", 0.8);
        double mythic = weight(plugin, "mythic", 0.2);
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
            case COMMON -> createLootStack(randomMaterial(commonPool(type, environment)), random, 1, 8);
            case UNCOMMON -> random.nextDouble() < 0.12
                    ? vanillaEnchantedBook(random)
                    : createLootStack(randomMaterial(uncommonPool(type, environment)), random, 1, 5);
            case RARE -> random.nextDouble() < 0.42
                    ? createLootStack(randomMaterial(dimensionRarePool(environment)), random, 1, 2)
                    : random.nextBoolean()
                    ? enchantedGear(random)
                    : createLootStack(randomMaterial(List.of("DIAMOND", "GOLDEN_APPLE", "ENDER_EYE",
                    "AMETHYST_SHARD", "EXPERIENCE_BOTTLE", "POTION", "SPLASH_POTION")), random, 1, 2);
            case EPIC -> random.nextBoolean()
                    ? customEnchantmentTome(plugin, random)
                    : artifact(plugin, random, "Epic");
            case LEGENDARY -> artifact(plugin, random, "Legendary");
            case MYTHIC -> artifact(plugin, random, "Mythic");
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
        List<String> pool = new ArrayList<>(familyCommonPool(type));
        // Broad utility mix: navigation, crafting, building and basic progression.
        pool.addAll(List.of(
                "PAPER", "CLOCK", "BOOK", "WRITTEN_BOOK", "FEATHER", "STRING",
                "IRON_NUGGET", "COAL", "COPPER_INGOT", "REDSTONE", "LAPIS_LAZULI",
                "GOLD_NUGGET", "BREAD", "ARROW", "LEATHER", "TORCH", "STICK",
                "APPLE", "CARROT", "POTATO", "GOLDEN_CARROT", "BAKED_POTATO",
                "BEETROOT", "BEETROOT_SEEDS", "MELON_SLICE", "PUMPKIN_SEEDS",
                "WHEAT", "WHEAT_SEEDS", "SUGAR", "HONEY_BOTTLE", "HONEYCOMB",
                "FIREWORK_ROCKET", "OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "DARK_OAK_LOG",
                "COBBLESTONE", "STONE", "DIRT", "COARSE_DIRT", "GRAVEL", "SAND",
                "RED_SAND", "CLAY_BALL", "GRANITE", "DIORITE", "ANDESITE", "TUFF",
                "CALCITE", "DEEPSLATE", "COBBLED_DEEPSLATE", "MOSS_BLOCK", "MUD",
                "GLASS", "FLINT", "BRICK", "BONE", "BONE_MEAL", "INK_SAC",
                "GLOW_INK_SAC", "WHITE_DYE", "ORANGE_DYE", "MAGENTA_DYE",
                "LIGHT_BLUE_DYE", "YELLOW_DYE", "LIME_DYE", "PINK_DYE", "GRAY_DYE",
                "LIGHT_GRAY_DYE", "CYAN_DYE", "PURPLE_DYE", "BLUE_DYE", "BROWN_DYE",
                "GREEN_DYE", "RED_DYE", "BLACK_DYE", "LANTERN", "SOUL_LANTERN",
                "CANDLE", "CHAIN", "BARREL", "CHEST", "NAME_TAG", "LEAD",
                "SADDLE", "ITEM_FRAME", "GLOW_ITEM_FRAME", "ARMOR_STAND",
                "POTION", "SPLASH_POTION", "LINGERING_POTION")); 
        if (isExplorer(type.name())) pool.add("MAP");
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
                "GOLDEN_CARROT", "ENDER_PEARL", "BOOK", "COMPASS",
                "FLINT_AND_STEEL", "OBSIDIAN", "POTION", "SPLASH_POTION",
                "LINGERING_POTION", "LANTERN", "SOUL_LANTERN", "NAME_TAG",
                "SADDLE", "LEAD", "BELL", "HONEY_BOTTLE", "SLIME_BALL",
                "MAGMA_CREAM", "GLOW_INK_SAC", "WRITTEN_BOOK",
                "SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", "DUNE_ARMOR_TRIM_SMITHING_TEMPLATE",
                "COAST_ARMOR_TRIM_SMITHING_TEMPLATE", "WILD_ARMOR_TRIM_SMITHING_TEMPLATE",
                "WARD_ARMOR_TRIM_SMITHING_TEMPLATE", "EYE_ARMOR_TRIM_SMITHING_TEMPLATE",
                "VEX_ARMOR_TRIM_SMITHING_TEMPLATE", "TIDE_ARMOR_TRIM_SMITHING_TEMPLATE",
                "SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", "RIB_ARMOR_TRIM_SMITHING_TEMPLATE",
                "SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE", "WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE",
                "SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", "SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE",
                "RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", "HOST_ARMOR_TRIM_SMITHING_TEMPLATE",
                "FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", "BOLT_ARMOR_TRIM_SMITHING_TEMPLATE"));
        if (isExplorer(type.name())) pool.add("MAP");
        pool.addAll(dimensionCommonPool(environment));
        return pool;
    }

    private static boolean isExplorer(String typeName) {
        return List.of("VILLAGER", "WANDERING_TRADER", "TRADER_LLAMA",
                "DONKEY", "MULE", "LLAMA", "PARROT").contains(typeName);
    }

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
                    "ENDER_EYE", "ELYTRA", "END_CRYSTAL");
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
        meta.displayName(itemTitle("Enchanted " + title(item.getType().name()), NamedTextColor.AQUA));
        item.setItemMeta(meta);
        return item;
    }

    private record EnchantmentRange(String id, int minimum, int maximum) { }

    private static final List<EnchantmentRange> CUSTOM_ENCHANTMENTS = List.of(
            new EnchantmentRange("sharpness", 8, 12),
            new EnchantmentRange("smite", 8, 12),
            new EnchantmentRange("bane_of_arthropods", 8, 12),
            new EnchantmentRange("power", 8, 12),
            new EnchantmentRange("protection", 6, 8),
            new EnchantmentRange("blast_protection", 5, 7),
            new EnchantmentRange("fire_protection", 5, 7),
            new EnchantmentRange("projectile_protection", 5, 7),
            new EnchantmentRange("efficiency", 8, 10),
            new EnchantmentRange("unbreaking", 6, 9),
            new EnchantmentRange("fortune", 4, 6),
            new EnchantmentRange("looting", 4, 6),
            new EnchantmentRange("feather_falling", 6, 8),
            new EnchantmentRange("respiration", 4, 6),
            new EnchantmentRange("sweeping_edge", 4, 6),
            new EnchantmentRange("fire_aspect", 2, 4),
            new EnchantmentRange("knockback", 2, 4),
            new EnchantmentRange("depth_strider", 4, 5),
            new EnchantmentRange("thorns", 4, 5),
            new EnchantmentRange("soul_speed", 4, 5),
            new EnchantmentRange("swift_sneak", 4, 5),
            new EnchantmentRange("quick_charge", 3, 5),
            new EnchantmentRange("piercing", 4, 6),
            new EnchantmentRange("impaling", 6, 8),
            new EnchantmentRange("loyalty", 3, 5),
            new EnchantmentRange("riptide", 3, 5)
    );

    /**
     * This is a plugin-applied enchanted book, not a vanilla anvil book.
     * Keeping the high level in PDC rather than EnchantmentStorageMeta avoids
     * Minecraft's untranslated enchantment.level.N tooltip for levels > max.
     */
    private static ItemStack customEnchantmentTome(UltraOPMobChestsPlugin plugin,
                                                   ThreadLocalRandom random) {
        EnchantmentRange selected = CUSTOM_ENCHANTMENTS.get(random.nextInt(CUSTOM_ENCHANTMENTS.size()));
        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(selected.id()));
        if (enchantment == null) {
            selected = new EnchantmentRange("sharpness", 8, 10);
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(selected.id()));
        }

        int rank = random.nextInt(selected.minimum(), selected.maximum() + 1);
        ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = tome.getItemMeta();
        meta.displayName(itemTitle("Ancient Enchanted Book", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
                itemLore("Enchantment: " + title(selected.id()), NamedTextColor.GOLD),
                itemLore("Level: " + rank, NamedTextColor.AQUA),
                itemLore("Hold compatible gear in your off-hand.", NamedTextColor.GRAY),
                itemLore("Right-click while holding this book.", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_enchantment_rank"),
                PersistentDataType.INTEGER, rank);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_enchantment_id"),
                PersistentDataType.STRING, selected.id());
        tome.setItemMeta(meta);
        return tome;
    }

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
        if (material == Material.WRITTEN_BOOK) return signedBook(random);
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

    private static ItemStack signedBook(ThreadLocalRandom random) {
        String[] titles = {"Field Notes", "Wanderer's Journal", "Old Trading Notes",
                "Ruins Research", "Survival Notes", "Explorer's Log"};
        String[] pages = {
                "A few supplies were found nearby. Keep a compass and some food ready before travelling.",
                "The Nether is dangerous. Bring fire resistance, sturdy armour and a way to return.",
                "Ancient structures may contain useful templates, books and rare materials.",
                "A quiet record of a traveller's route. The map is incomplete, but the notes may help."
        };
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(titles[random.nextInt(titles.length)]);
        meta.setAuthor("Unknown Explorer");
        meta.setPages(List.of(pages[random.nextInt(pages.length)]));
        book.setItemMeta(meta);
        return book;
    }

    private static ItemStack customEquipment(UltraOPMobChestsPlugin plugin,
                                             ThreadLocalRandom random,
                                             World.Environment environment) {
        String[] ids = {"skybound_boots", "aetherguard_chestplate", "phantom_leggings",
                "stormcaller_bow", "infernal_edge", "voidwalker_helmet",
                "emerald_helmet", "emerald_chestplate", "emerald_leggings", "emerald_boots"};
        // Infernal Edge gets a Netherite base only inside the Nether.
        Material infernalEdgeMaterial = environment == World.Environment.NETHER
                ? Material.NETHERITE_SWORD : Material.DIAMOND_SWORD;
        Material[] materials = {Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS, Material.BOW, infernalEdgeMaterial,
                Material.DIAMOND_HELMET, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
        int index = random.nextInt(ids.length);
        ItemStack item = taggedArtifact(plugin, ids[index], title(ids[index]), materials[index]);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta leather) {
            leather.setColor(Color.fromRGB(47, 190, 91));
            leather.displayName(itemTitle(title(ids[index]), NamedTextColor.GREEN));
            item.setItemMeta(leather);
        }
        return item;
    }

    private static ItemStack goldenFood(UltraOPMobChestsPlugin plugin, ThreadLocalRandom random) {
        String[] ids = {"golden_potato", "golden_porkchop", "golden_bread"};
        String[] names = {"Golden Potato", "Golden Porkchop", "Golden Bread"};
        Material[] materials = {Material.BAKED_POTATO, Material.COOKED_PORKCHOP, Material.BREAD};
        int index = random.nextInt(ids.length);
        ItemStack item = taggedArtifact(plugin, ids[index], names[index], materials[index]);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(itemTitle(names[index], NamedTextColor.GOLD));
        meta.lore(List.of(itemLore("Rare golden food", NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack liquidGoldBucket(UltraOPMobChestsPlugin plugin) {
        ItemStack item = taggedArtifact(plugin, "liquid_gold_bucket",
                "Liquid Gold Bucket", Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_fluid_id"),
                PersistentDataType.STRING, "liquid_gold");
        meta.displayName(itemTitle("Liquid Gold Bucket", NamedTextColor.GOLD));
        meta.lore(List.of(
                itemLore("Golden water-like liquid", NamedTextColor.GRAY),
                itemLore("This item is sealed in the current build.", NamedTextColor.DARK_GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack artifact(UltraOPMobChestsPlugin plugin,
                                      ThreadLocalRandom random, String rarityLabel) {
        String[] ids = {"skull_scepter", "abyssal_heart", "warden_echo_relic",
                "dragon_wing_plate", "dragon_blast"};
        String[] names = {"Skull Scepter", "Abyssal Heart", "Warden Echo Relic",
                "Dragon Wing Plate", "Dragon Blast"};
        Material[] materials = {Material.BLAZE_ROD, Material.HEART_OF_THE_SEA,
                Material.ECHO_SHARD, Material.ELYTRA, Material.FIRE_CHARGE};
        int index = random.nextInt(ids.length);
        ItemStack item = taggedArtifact(plugin, ids[index], names[index], materials[index]);
        ItemMeta meta = item.getItemMeta();
        String description = switch (ids[index]) {
            case "skull_scepter" -> "A scepter carved from a cursed skull.";
            case "abyssal_heart" -> "A rare relic recovered from the deep.";
            case "warden_echo_relic" -> "A resonant relic from the Deep Dark.";
            case "dragon_wing_plate" -> "A plate forged from dragon-scale metal.";
            case "dragon_blast" -> "A volatile charge infused with dragon fire.";
            default -> "A rare enchanted relic.";
        };
        meta.displayName(itemTitle(names[index], NamedTextColor.GOLD));
        meta.lore(List.of(itemLore(description, NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack taggedArtifact(UltraOPMobChestsPlugin plugin, String id,
                                            String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(itemTitle(name, NamedTextColor.GOLD));
        meta.lore(List.of(itemLore("Rare treasure", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_item_id"),
                PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack stack(Material material, ThreadLocalRandom random, int min, int max) {
        if (material == null || material.isAir()) material = Material.BREAD;
        int maxAllowed = Math.max(1, material.getMaxStackSize());
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

    private enum Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC }
}
