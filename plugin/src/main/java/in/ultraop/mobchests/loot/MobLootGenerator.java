package in.ultraop.mobchests.loot;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mob-family loot generator. The current interaction prototype calls this for
 * Cows; profiles cover vanilla mob families for later visual-model expansion.
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

    public static Inventory create(java.util.UUID mobId, EntityType type,
                                   UltraOPMobChestsPlugin plugin) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        MobChestInventoryHolder holder = new MobChestInventoryHolder(mobId);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE,
                Component.text(displayName(type.name()) + " Treasure", NamedTextColor.GOLD));
        holder.bind(inventory);

        int minStacks = clamp(plugin.getConfig().getInt("loot.minimum-item-stacks", 4), 0, 27);
        int maxStacks = clamp(plugin.getConfig().getInt("loot.maximum-item-stacks", 8), minStacks, 27);
        int stacks = random.nextInt(minStacks, maxStacks + 1);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) slots.add(slot);
        Collections.shuffle(slots, random);

        for (int i = 0; i < stacks; i++) {
            inventory.setItem(slots.get(i), reward(type, rollRarity(type, plugin, random), plugin, random));
        }

        // One custom equipment roll maximum. It replaces a normal slot.
        double gearChance = Math.max(0.0, Math.min(1.0,
                plugin.getConfig().getDouble("loot.custom-equipment-chance", 0.10)));
        if (stacks > 0 && random.nextDouble() < gearChance) {
            inventory.setItem(slots.get(random.nextInt(stacks)), customEquipment(plugin, random));
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

    private static ItemStack reward(EntityType type, Rarity rarity,
                                    UltraOPMobChestsPlugin plugin,
                                    ThreadLocalRandom random) {
        return switch (rarity) {
            case COMMON -> stack(randomMaterial(commonPool(type)), random, 1, 5);
            case UNCOMMON -> stack(randomMaterial(List.of(
                    "IRON_INGOT", "GOLD_INGOT", "EMERALD", "EXPERIENCE_BOTTLE",
                    "GOLDEN_CARROT", "ENDER_PEARL")), random, 1, 3);
            case RARE -> random.nextBoolean()
                    ? enchantedGear(random)
                    : stack(randomMaterial(List.of("DIAMOND", "GOLDEN_APPLE", "ENDER_EYE",
                    "NETHERITE_SCRAP", "AMETHYST_SHARD")), random, 1, 2);
            case EPIC -> random.nextBoolean()
                    ? customEnchantmentTome(plugin, random)
                    : artifact(plugin, random, "Epic");
            case LEGENDARY -> artifact(plugin, random, "Legendary");
            case MYTHIC -> artifact(plugin, random, "Mythic");
        };
    }

    private static List<String> commonPool(EntityType type) {
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
        meta.displayName(Component.text("Enchanted " + title(item.getType().name()), NamedTextColor.AQUA));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack customEnchantmentTome(UltraOPMobChestsPlugin plugin,
                                                   ThreadLocalRandom random) {
        ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
        int rank = random.nextInt(30, 61);
        ItemMeta meta = tome.getItemMeta();
        meta.displayName(Component.text("UltraOP Enchantment Tome", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
                Component.text("Custom enchantment rank: " + rank, NamedTextColor.GOLD),
                Component.text("Separate from vanilla enchantment levels.", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_enchantment_rank"),
                PersistentDataType.INTEGER, rank);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_enchantment_id"),
                PersistentDataType.STRING, "random_tome");
        tome.setItemMeta(meta);
        return tome;
    }

    private static ItemStack customEquipment(UltraOPMobChestsPlugin plugin,
                                             ThreadLocalRandom random) {
        String[] ids = {"skybound_boots", "aetherguard_chestplate", "phantom_leggings",
                "stormcaller_bow", "infernal_edge", "voidwalker_helmet"};
        Material[] materials = {Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS, Material.BOW, Material.NETHERITE_SWORD,
                Material.DIAMOND_HELMET};
        int index = random.nextInt(ids.length);
        return taggedArtifact(plugin, ids[index], title(ids[index]), materials[index]);
    }

    private static ItemStack artifact(UltraOPMobChestsPlugin plugin,
                                      ThreadLocalRandom random, String rarityLabel) {
        String[] ids = {"liquid_gold_bucket", "skull_scepter", "abyssal_heart",
                "warden_echo_relic", "dragon_wing_plate", "dragon_blast"};
        String[] names = {"Liquid Gold Bucket", "Skull Scepter", "Abyssal Heart",
                "Warden Echo Relic", "Dragon Wing Plate", "Dragon Blast"};
        Material[] materials = {Material.BUCKET, Material.BLAZE_ROD, Material.HEART_OF_THE_SEA,
                Material.ECHO_SHARD, Material.ELYTRA, Material.FIRE_CHARGE};
        int index = random.nextInt(ids.length);
        ItemStack item = taggedArtifact(plugin, ids[index], names[index], materials[index]);
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text(rarityLabel + " UltraOP Artifact",
                "Mythic".equals(rarityLabel) ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.DARK_PURPLE),
                Component.text("Unique artifact ID: " + ids[index], NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack taggedArtifact(UltraOPMobChestsPlugin plugin, String id,
                                            String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GOLD));
        meta.lore(List.of(Component.text("UltraOP custom item", NamedTextColor.DARK_PURPLE)));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_item_id"),
                PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack stack(Material material, ThreadLocalRandom random, int min, int max) {
        if (material == null || material.isAir()) material = Material.BREAD;
        int upper = Math.min(max, material.getMaxStackSize());
        return new ItemStack(material, random.nextInt(min, Math.max(min, upper) + 1));
    }

    private static Material randomMaterial(List<String> names) {
        Material material = Material.matchMaterial(names.get(
                ThreadLocalRandom.current().nextInt(names.size())));
        return material == null ? Material.BREAD : material;
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
