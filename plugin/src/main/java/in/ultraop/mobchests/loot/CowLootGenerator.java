package in.ultraop.mobchests.loot;

import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Temporary Cow loot table used by the animation prototype.
 * The full data-driven loot engine is a separate roadmap phase.
 */
public final class CowLootGenerator {
    private CowLootGenerator() {
    }

    public static Inventory create(java.util.UUID mobId) {
        MobChestInventoryHolder holder = new MobChestInventoryHolder(mobId);
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Cow Treasure"));
        holder.bind(inventory);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int stacks = random.nextInt(4, 9);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            slots.add(slot);
        }
        java.util.Collections.shuffle(slots, random);

        for (int i = 0; i < stacks; i++) {
            ItemStack reward = createReward(random);
            inventory.setItem(slots.get(i), reward);
        }
        return inventory;
    }

    private static ItemStack createReward(ThreadLocalRandom random) {
        int roll = random.nextInt(100);
        if (roll < 34) {
            return new ItemStack(Material.BEEF, random.nextInt(1, 5));
        }
        if (roll < 54) {
            return new ItemStack(Material.LEATHER, random.nextInt(1, 4));
        }
        if (roll < 68) {
            return new ItemStack(Material.WHEAT, random.nextInt(1, 7));
        }
        if (roll < 80) {
            return new ItemStack(Material.APPLE, random.nextInt(1, 3));
        }
        if (roll < 90) {
            return new ItemStack(Material.BREAD, random.nextInt(1, 4));
        }
        if (roll < 97) {
            return new ItemStack(Material.GOLDEN_CARROT, 1);
        }
        return new ItemStack(Material.GOLDEN_APPLE, 1);
    }
}
