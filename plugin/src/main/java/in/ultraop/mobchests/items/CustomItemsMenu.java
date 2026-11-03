package in.ultraop.mobchests.items;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class CustomItemsMenu implements Listener {
    private final UltraOPMobChestsPlugin plugin;

    public CustomItemsMenu(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27,
                Component.text("UltraOP | MobChests Items", NamedTextColor.GOLD));
        holder.inventory = inventory;

        List<ItemStack> items = CustomItemFactory.createCatalogue(plugin);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24};
        for (int i = 0; i < items.size() && i < slots.length; i++) {
            inventory.setItem(slots[i], items.get(i));
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() != event.getView().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(clicked.clone());
        leftovers.values().forEach(stack ->
                player.getWorld().dropItemNaturally(player.getLocation(), stack));
        player.sendMessage(Component.text("Added " + clicked.getAmount() + " × "
                + clicked.getType().name().toLowerCase().replace('_', ' ') + ".", NamedTextColor.GREEN));
    }

    private static final class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
