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
    private static final int SINGLE_CHEST_SIZE = 27;

    /** Every mob uses a compact single chest. */
    public static int inventorySize(EntityType type) { return SINGLE_CHEST_SIZE; }
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
        Inventory inventory = Bukkit.createInventory(holder, inventorySize(type),
                Component.text(displayName(type.name()) + " Treasure", NamedTextColor.GOLD));
        holder.bind(inventory);

        // Loot quantity scales by threat: tiny/passive mobs stay modest, while
        // combat mobs and bosses always have something worth opening the chest for.
        int configuredMaximum = clamp(plugin.getConfig().getInt("loot.maximum-item-stacks", 6), 1, 6);
        int[] range = switch (tier(type)) {
            case COMMON -> new int[]{3, 4};
            case MID -> new int[]{2, 4};
            case STRONG -> new int[]{5, 6};
            case BOSS -> new int[]{5, 6};
        };
        int minStacks = Math.min(range[0], configuredMaximum);
        int maxStacks = Math.min(range[1], configuredMaximum);
        int stacks = random.nextInt(minStacks, maxStacks + 1);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) slots.add(slot);
        Collections.shuffle(slots, random);

        boolean trimAlreadyGenerated = false;
        for (int i = 0; i < stacks; i++) {
            ItemStack generated = reward(type, environment, rollRarity(type, plugin, random),
                    plugin, random, !trimAlreadyGenerated);
            inventory.setItem(slots.get(i), generated);
            if (generated.getType().name().endsWith("_ARMOR_TRIM_SMITHING_TEMPLATE")) {
                trimAlreadyGenerated = true;
            }
        }

        int nextSpecialSlot = stacks;
        double gearChance = switch (tier(type)) {
            case COMMON -> chance(plugin, "loot.tier-equipment-chance.common", 0.01);
            case MID -> chance(plugin, "loot.tier-equipment-chance.mid", 0.04);
            case STRONG -> chance(plugin, "loot.tier-equipment-chance.strong", 0.12);
            case BOSS -> chance(plugin, "loot.tier-equipment-chance.boss", 0.35);
        };
        // Bat Elytra chance is independent of the normal roll count, replacing one slot if needed.
        if (type == EntityType.BAT
                && random.nextDouble() < chance(plugin, "loot.bat-elytra-chance", 0.02)) {
            inventory.setItem(slots.get(0), new ItemStack(Material.ELYTRA, 1));
        } else if (stacks > 0 && random.nextDouble() < gearChance) {
            // Special gear replaces a normal item in a shuffled slot.
            int gearSlot = nextSpecialSlot < 4 ? nextSpecialSlot : random.nextInt(Math.min(stacks, 4));
            inventory.setItem(slots.get(gearSlot), tierEquipment(type, random));
        }

        // Extraordinary OP gear is a separate, very rare reward for powerful mobs only.
        double opChance = switch (tier(type)) {
            case COMMON, MID -> 0.0;
            case STRONG -> chance(plugin, "loot.rare-op-item-chance.strong", 0.10);
            case BOSS -> chance(plugin, "loot.rare-op-item-chance.boss", 0.35);
        };
        if (random.nextDouble() < opChance) {
            inventory.setItem(slots.get(random.nextInt(Math.min(stacks, 4))), extraordinaryOpItem(random));
        }

        // Every Overworld mob chest contributes one essential progression item.
        // This keeps portal ignition and the Nether/End progression accessible even
        // when players do not receive these items from normal vanilla mob drops.
        if (environment == World.Environment.NORMAL && stacks > 0) {
            inventory.setItem(slots.get(stacks - 1), overworldProgressionItem(random));
        }

        // Warden chests always contain two enchanted armor pieces and one enchanted weapon.
        // Each piece has a 35% OP-enchantment roll; guarantee at least one and cap at two.
        if (type == EntityType.WARDEN) {
            Material[] armorPieces = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                    Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
            for (int i = armorPieces.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Material swap = armorPieces[i];
                armorPieces[i] = armorPieces[j];
                armorPieces[j] = swap;
            }
            Material[] wardenGear = {armorPieces[0], armorPieces[1],
                    new Material[]{Material.NETHERITE_SWORD, Material.NETHERITE_AXE,
                            Material.DIAMOND_SPEAR, Material.TRIDENT, Material.BOW,
                            Material.CROSSBOW}[random.nextInt(6)]};
            boolean[] opGear = new boolean[wardenGear.length];
            int opCount = 0;
            for (int i = 0; i < opGear.length; i++) {
                opGear[i] = random.nextDouble() < 0.35;
                if (opGear[i]) opCount++;
            }
            if (opCount == 0) {
                opGear[random.nextInt(opGear.length)] = true;
            } else if (opCount == 3) {
                opGear[random.nextInt(opGear.length)] = false;
            }
            for (int i = 0; i < wardenGear.length; i++) {
                inventory.setItem(slots.get(i), enchantedWardenGear(wardenGear[i], random, opGear[i]));
            }
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
                                    UltraOPMobChestsPlugin plugin,                                    ThreadLocalRandom random, boolean allowTrim) {
        if (tier(type) == MobTier.STRONG || tier(type) == MobTier.BOSS) return powerfulMobReward(type, random, rarity, allowTrim);
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

    /**
     * Strong mobs and bosses provide useful progression materials in several
     * distinct stacks. Netherite and trim rewards remain deliberately scarce.
     */
    private static ItemStack powerfulMobReward(EntityType type, ThreadLocalRandom random, Rarity rarity, boolean allowTrim) {
        boolean boss = tier(type) == MobTier.BOSS;
        int roll = random.nextInt(1000);
        if (allowTrim && roll < (boss ? 3 : 1)) {
            return new ItemStack(randomMaterial(ARMOR_TRIM_ITEMS), 1);
        }
        if (roll < (boss ? 18 : 7)) {
            Material[] netheriteGear = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                    Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
            ItemStack gear = new ItemStack(netheriteGear[random.nextInt(netheriteGear.length)], 1);
            enchantUsefulGear(gear, random, boss ? 10 : 8, 3);
            return gear;
        }
        if (roll < (boss ? 48 : 18)) {
            return new ItemStack(Material.NETHERITE_INGOT, random.nextInt(1, 3));
        }
        if (roll < (boss ? 145 : 85)) {
            Material[] diamondGear = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD,
                    Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE};
            Material selected = diamondGear[random.nextInt(diamondGear.length)];
            ItemStack gear = new ItemStack(selected, 1);
            enchantUsefulGear(gear, random, boss ? 10 : 8, 3);
            return gear;
        }

        // Core rewards: random useful stacks, with larger quantities for valuable consumables.
        int choice = random.nextInt(24);
        return switch (choice) {
            case 0 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, random.nextInt(5, 11));
            case 1 -> new ItemStack(Material.GOLD_BLOCK, random.nextInt(2, 6));
            case 2 -> new ItemStack(Material.DIAMOND_BLOCK, random.nextInt(1, 3));
            case 3 -> new ItemStack(Material.GOLD_INGOT, random.nextInt(8, 25));
            case 4 -> new ItemStack(Material.DIAMOND, random.nextInt(3, 9));
            case 5 -> new ItemStack(Material.EMERALD_BLOCK, random.nextInt(1, 4));
            case 6 -> new ItemStack(Material.EXPERIENCE_BOTTLE, random.nextInt(8, 25));
            case 7 -> new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            case 8 -> new ItemStack(Material.NETHERITE_SCRAP, random.nextInt(1, 4));
            case 9 -> new ItemStack(Material.ANCIENT_DEBRIS, random.nextInt(1, 3));
            case 10 -> new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
            case 11 -> vanillaEnchantedBook(random);
            case 12 -> new ItemStack(Material.GOLDEN_CARROT, random.nextInt(8, 17));
            case 13 -> new ItemStack(Material.IRON_BLOCK, random.nextInt(3, 9));
            case 14 -> createLootStack(randomMaterial(List.of("FIREWORK_ROCKET", "ARROW", "SPECTRAL_ARROW")), random, 8, 24);
            case 15 -> createLootStack(randomMaterial(List.of("ENDER_PEARL", "ENDER_EYE", "BLAZE_ROD")), random, 2, 6);
            case 16 -> createLootStack(randomMaterial(List.of("GOAT_HORN", "MUSIC_DISC_13", "MUSIC_DISC_CAT", "MUSIC_DISC_PIGSTEP")), random, 1, 1);
            case 17 -> createLootStack(randomMaterial(List.of("LANTERN", "CHAIN", "LADDER", "CANDLE", "AMETHYST_SHARD")), random, 2, 8);
            case 18 -> createLootStack(randomMaterial(List.of("WHITE_WOOL", "GLASS", "SAND", "STONE", "COBBLED_DEEPSLATE")), random, 8, 24);
            case 19 -> createLootStack(randomMaterial(List.of("OAK_PLANKS", "SPRUCE_PLANKS", "OAK_FENCE_GATE", "IRON_BARS")), random, 4, 16);
            case 20 -> {
                ItemStack spear = createLootStack(randomMaterial(List.of("GOLDEN_SPEAR", "IRON_SPEAR", "DIAMOND_SPEAR")), random, 1, 1);
                if (spear.getType() == Material.DIAMOND_SPEAR) enchantUsefulGear(spear, random, 8, 3);
                yield spear;
            }
            case 21 -> createLootStack(randomMaterial(List.of("TOTEM_OF_UNDYING", "HONEY_BOTTLE", "MILK_BUCKET")), random, 1, 1);
            case 22 -> createLootStack(randomMaterial(List.of("TNT", "FIRE_CHARGE", "BLAZE_POWDER")), random, 2, 8);
            default -> new ItemStack(Material.ECHO_SHARD, random.nextInt(2, 5));
        };
    }

    private static List<String> familyCommonPool(EntityType type) {
        String name = type.name();
        if (name.equals("COW")) return List.of("BEEF", "LEATHER", "WHEAT", "BREAD", "APPLE", "MILK_BUCKET");
        if (name.equals("PIG") || name.equals("HOGLIN") || name.equals("ZOGLIN")) {
            return List.of("PORKCHOP", "CARROT", "POTATO", "LEATHER", "GOLD_NUGGET");
        }
        if (name.equals("SHEEP")) return List.of("MUTTON", "WHITE_WOOL", "STRING", "WHEAT");
        if (name.equals("PARROT")) return List.of("FEATHER", "WHEAT_SEEDS", "MELON_SEEDS",
                "PUMPKIN_SEEDS", "COCOA_BEANS", "GOLD_NUGGET");
        if (name.equals("CHICKEN")) return List.of("CHICKEN", "FEATHER", "EGG", "WHEAT_SEEDS");
        if (name.contains("SKELETON") || name.equals("BOGGED")) return SKELETON_COMMON;
        if (name.equals("ZOMBIE") || name.equals("DROWNED") || name.equals("HUSK")
                || name.equals("ZOMBIFIED_PIGLIN")) return UNDEAD_COMMON;
        if (name.equals("SPIDER") || name.equals("CAVE_SPIDER")) {
            return List.of("STRING", "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "BROWN_MUSHROOM");
        }
        if (name.equals("CREEPER")) return List.of("GUNPOWDER", "TNT", "FIRE_CHARGE", "COAL", "REDSTONE", "FIREWORK_ROCKET");
        if (name.equals("ENDERMAN") || name.equals("ENDERMITE") || name.equals("SHULKER")) {
            return List.of("ENDER_PEARL", "ENDER_EYE", "CHORUS_FRUIT", "PURPUR_BLOCK", "OBSIDIAN", "FIRE_CHARGE");
        }
        if (name.equals("BEE")) return List.of("HONEY_BOTTLE", "HONEYCOMB", "FLOWER", "CANDLE");
        if (name.equals("GOAT")) return List.of("GOAT_HORN", "WHEAT", "LEATHER", "WHITE_WOOL");
        if (name.equals("BLAZE") || name.equals("GHAST") || name.equals("MAGMA_CUBE")
                || name.equals("PIGLIN") || name.equals("PIGLIN_BRUTE")) return NETHER_COMMON;
        if (name.contains("GUARDIAN") || name.equals("DOLPHIN") || name.equals("SQUID")
                || name.equals("GLOW_SQUID") || name.equals("TURTLE") || name.equals("COD")
                || name.equals("SALMON") || name.equals("PUFFERFISH") || name.equals("TROPICAL_FISH")) {
            return AQUATIC_COMMON;
        }
        if (name.equals("WARDEN")) return List.of("ECHO_SHARD", "SCULK_CATALYST", "DIAMOND",
                "EMERALD", "GOLDEN_APPLE", "EXPERIENCE_BOTTLE", "DIAMOND_SWORD", "BOW");
        if (name.equals("ENDER_DRAGON") || name.equals("WITHER")) {
            return List.of("DIAMOND", "GOLD_INGOT", "EXPERIENCE_BOTTLE", "ENDER_PEARL");
        }
        if (isPassive(name)) return PASSIVE_COMMON;
        return GENERIC_COMMON;
    }

    private static List<String> commonPool(EntityType type, World.Environment environment) {
        // Passive creatures use a tighter species pool so they do not receive
        // random terrain blocks or combat-only loot.
        if (tier(type) == MobTier.COMMON) {
            return new ArrayList<>(familyCommonPool(type));
        }
        // Keep combat drops practical and compact.
        List<String> pool = new ArrayList<>(familyCommonPool(type));
        pool.addAll(List.of(
                "COAL", "IRON_NUGGET", "COPPER_INGOT", "REDSTONE", "LAPIS_LAZULI",
                "ARROW", "SPECTRAL_ARROW", "FIREWORK_ROCKET", "STRING", "STICK", "TORCH", "BREAD", "APPLE",
                "CARROT", "POTATO", "WHEAT", "LEATHER", "TNT", "ENDER_PEARL", "BLAZE_ROD",
                "LANTERN", "CHAIN", "LADDER", "CANDLE", "AMETHYST_SHARD", "WHITE_WOOL",
                "GLASS", "SAND", "OAK_PLANKS", "OAK_FENCE_GATE", "GOAT_HORN"));
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
        String name = type.name();        if (isBoss(type) || name.equals("WARDEN")) return MobTier.BOSS;
        if (List.of("RAVAGER", "EVOKER", "VINDICATOR", "PIGLIN_BRUTE", "IRON_GOLEM", "BLAZE",
                "GHAST", "SHULKER", "HOGLIN", "ZOGLIN").contains(name)) return MobTier.STRONG;
        if (List.of("ZOMBIE", "HUSK", "DROWNED", "SKELETON", "STRAY", "CREEPER",
                "SPIDER", "CAVE_SPIDER", "ENDERMAN", "WITCH", "PHANTOM", "PILLAGER",
                "GUARDIAN", "PIGLIN", "BREEZE", "SLIME", "MAGMA_CUBE", "VEX",
                "SILVERFISH", "ENDERMITE").contains(name)) return MobTier.MID;
        return MobTier.COMMON;
    }

    private static ItemStack extraordinaryOpItem(ThreadLocalRandom random) {
        int level = random.nextInt(15, 19);
        return switch (random.nextInt(4)) {
            case 0, 1 -> {
                ItemStack sword = new ItemStack(random.nextBoolean() ? Material.NETHERITE_SWORD : Material.DIAMOND_SWORD);
                ItemMeta meta = sword.getItemMeta();
                Enchantment sharpness = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness"));
                Enchantment looting = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("looting"));
                if (sharpness != null) meta.addEnchant(sharpness, level, true);
                if (looting != null) meta.addEnchant(looting, random.nextInt(5, 9), true);
                sword.setItemMeta(meta);
                yield sword;
            }
            case 2 -> {
                ItemStack bow = new ItemStack(Material.BOW);
                ItemMeta meta = bow.getItemMeta();
                Enchantment power = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("power"));
                if (power != null) meta.addEnchant(power, level, true);
                bow.setItemMeta(meta);
                yield bow;
            }
            default -> {
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                ItemMeta meta = crossbow.getItemMeta();
                Enchantment quick = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("quick_charge"));
                Enchantment piercing = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("piercing"));
                if (quick != null) meta.addEnchant(quick, level, true);
                if (piercing != null) meta.addEnchant(piercing, level, true);
                crossbow.setItemMeta(meta);
                yield crossbow;
            }
        };
    }

    private static ItemStack tierEquipment(EntityType type, ThreadLocalRandom random) {
        String name = type.name();
        boolean boss = isBoss(type) || name.equals("WARDEN");
        boolean strong = boss || List.of("RAVAGER", "EVOKER", "VINDICATOR", "PIGLIN_BRUTE",
                "IRON_GOLEM", "BLAZE", "GHAST", "SHULKER", "HOGLIN", "ZOGLIN").contains(name);
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
        int count = boss ? random.nextInt(2, 4) : strong ? random.nextInt(2, 4) : mid ? 2 : 1;
        int max = boss ? 12 : strong ? 10 : mid ? 5 : 2;
        if (name.equals("WARDEN") && selected.name().matches(".*_(HELMET|CHESTPLATE|LEGGINGS|BOOTS)$")) count = 0;
        for (int i = 0; i < count; i++) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(keys[random.nextInt(keys.length)]));
            if (enchantment != null) meta.addEnchant(enchantment, random.nextInt(1, max + 1), true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack overworldProgressionItem(ThreadLocalRandom random) {
        return switch (random.nextInt(100)) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39 ->
                    new ItemStack(Material.OBSIDIAN, random.nextInt(2, 5));
            case 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 ->
                    new ItemStack(Material.FLINT_AND_STEEL, 1);
            case 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 ->
                    new ItemStack(Material.BLAZE_ROD, random.nextInt(1, 3));
            default -> new ItemStack(Material.ENDER_EYE, random.nextInt(1, 3));
        };
    }

    private static ItemStack enchantedWardenGear(Material material, ThreadLocalRandom random, boolean overpowered) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        String name = material.name();
        String[] keys = name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                ? new String[]{"protection", "unbreaking", "thorns", "mending"}
                : name.contains("SPEAR") || name.contains("SWORD") || name.contains("AXE")
                ? new String[]{"sharpness", "looting", "fire_aspect", "unbreaking", "mending"}
                : name.equals("TRIDENT")
                ? new String[]{"impaling", "loyalty", "channeling", "unbreaking", "mending"}
                : name.contains("CROSSBOW")
                ? new String[]{"quick_charge", "piercing", "multishot", "unbreaking", "mending"}
                : new String[]{"power", "flame", "punch", "unbreaking", "mending"};
        int count = overpowered ? random.nextInt(3, 5) : random.nextInt(2, 4);
        for (int i = 0; i < count; i++) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(
                    NamespacedKey.minecraft(keys[random.nextInt(keys.length)]));
            if (enchantment != null) {
                int level = overpowered ? random.nextInt(10, 16)
                        : random.nextInt(1, Math.max(1, enchantment.getMaxLevel()) + 1);
                meta.addEnchant(enchantment, level, true);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    private static void enchantUsefulGear(ItemStack item, ThreadLocalRandom random, int maxLevel, int maxEnchantments) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String name = item.getType().name();
        String[] keys = name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
                ? new String[]{"protection", "unbreaking", "thorns", "mending"}
                : name.contains("SPEAR") || name.contains("SWORD") || name.contains("AXE")
                ? new String[]{"sharpness", "looting", "fire_aspect", "unbreaking", "mending"}
                : name.contains("BOW") ? new String[]{"power", "flame", "punch", "unbreaking", "mending"}
                : name.contains("CROSSBOW") ? new String[]{"quick_charge", "piercing", "multishot", "unbreaking", "mending"}
                : name.equals("TRIDENT") ? new String[]{"impaling", "loyalty", "channeling", "unbreaking", "mending"}
                : new String[]{"efficiency", "fortune", "unbreaking", "mending"};
        int count = random.nextInt(1, maxEnchantments + 1);
        for (int i = 0; i < count; i++) {
            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(keys[random.nextInt(keys.length)]));
            if (enchantment != null) meta.addEnchant(enchantment, random.nextInt(1, maxLevel + 1), true);
        }
        item.setItemMeta(meta);
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