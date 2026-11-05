package in.ultraop.mobchests.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class MobChestInventoryHolder implements InventoryHolder {
    private final UUID mobId;
    private Inventory inventory;

    public MobChestInventoryHolder(UUID mobId) {
        this.mobId = mobId;
    }

    public UUID mobId() {
        return mobId;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
