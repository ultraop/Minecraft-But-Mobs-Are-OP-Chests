package in.ultraop.mobchests.items;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.fluid.LiquidGoldService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CustomItemFactory {
    private record Food(String id, String name, Material base) { }

    private static final List<Food> FOODS = List.of(
            new Food("golden_potato", "Golden Potato", Material.POTATO),
            new Food("golden_baked_potato", "Golden Baked Potato", Material.BAKED_POTATO),
            new Food("golden_steak", "Golden Steak", Material.COOKED_BEEF),
            new Food("golden_porkchop", "Golden Porkchop", Material.COOKED_PORKCHOP),
            new Food("golden_chicken", "Golden Cooked Chicken", Material.COOKED_CHICKEN),
            new Food("golden_mutton", "Golden Cooked Mutton", Material.COOKED_MUTTON),
            new Food("golden_rabbit", "Golden Cooked Rabbit", Material.COOKED_RABBIT),
            new Food("golden_bread", "Golden Bread", Material.BREAD),
            new Food("golden_carrot", "Golden Carrot", Material.CARROT),
            new Food("golden_cod", "Golden Cooked Cod", Material.COOKED_COD),
            new Food("golden_salmon", "Golden Cooked Salmon", Material.COOKED_SALMON),
            new Food("golden_pumpkin_pie", "Golden Pumpkin Pie", Material.PUMPKIN_PIE)
    );

    private CustomItemFactory() { }

    public static List<String> foodIds() {
        return FOODS.stream().map(Food::id).toList();
    }

    public static List<ItemStack> createCatalogue(UltraOPMobChestsPlugin plugin) {
        List<ItemStack> items = new ArrayList<>();
        items.add(LiquidGoldService.createBucket(plugin));
        for (Food food : FOODS) items.add(createFood(plugin, food));
        return items;
    }

    public static ItemStack create(UltraOPMobChestsPlugin plugin, String id) {
        if ("liquid_gold_bucket".equalsIgnoreCase(id)) {
            return LiquidGoldService.createBucket(plugin);
        }
        return FOODS.stream()
                .filter(food -> food.id().equalsIgnoreCase(id))
                .findFirst()
                .map(food -> createFood(plugin, food))
                .orElse(null);
    }

    public static ItemStack createRandomFood(UltraOPMobChestsPlugin plugin,
                                             ThreadLocalRandom random) {
        Food food = FOODS.get(random.nextInt(FOODS.size()));
        return createFood(plugin, food);
    }

    private static ItemStack createFood(UltraOPMobChestsPlugin plugin, Food food) {
        ItemStack item = new ItemStack(food.base());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(food.name(), NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .decoration(TextDecoration.BOLD, TextDecoration.State.TRUE));
        meta.lore(List.of(
                Component.text("A familiar Minecraft food with a soft golden tint.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Eat for a random bonus lasting 5–10 seconds.", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        meta.setFood(food);
        meta.setItemModel(new NamespacedKey(plugin, food.id()));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_item_id"),
                PersistentDataType.STRING, food.id());
        item.setItemMeta(meta);
        return item;
    }
}
