package in.ultraop.mobchests.animation;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

/** Resource-pack-backed split-body visual prototype for adult Cows. */
public final class CowChestAnimator {
    private static final float OPEN_ANGLE = (float) Math.toRadians(-72.0);
    private static final float MODEL_SCALE = 1.0F;
    private final UltraOPMobChestsPlugin plugin;
    private final NamespacedKey ownerKey;

    public CowChestAnimator(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "animation_owner");
    }

    public AnimationPair create(Cow cow) {
        Location center = cow.getLocation().clone().add(0.0, 0.5, 0.0);
        UUID owner = cow.getUniqueId();
        ItemDisplay lower = spawnPart(center, "cow_base", owner, cow.getYaw());
        ItemDisplay upper = null;
        try {
            upper = spawnPart(center, "cow_lid", owner, cow.getYaw());
            return new AnimationPair(lower, upper);
        } catch (RuntimeException exception) {
            lower.remove();
            if (upper != null && upper.isValid()) upper.remove();
            throw exception;
        }
    }

    private ItemDisplay spawnPart(Location location, String model, UUID owner, float yaw) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.setItemModel(new NamespacedKey(plugin, model));
        stack.setItemMeta(meta);

        return location.getWorld().spawn(location, ItemDisplay.class, display -> {
            display.setItemStack(stack);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setBillboard(Display.Billboard.FIXED);
            display.setPersistent(false);
            display.setInvulnerable(true);
            display.setSilent(true);
            display.setShadowRadius(0.0F);
            display.setShadowStrength(0.0F);
            display.setViewRange(1.0F);
            display.setRotation(yaw, 0.0F);
            display.getPersistentDataContainer().set(ownerKey,
                    org.bukkit.persistence.PersistentDataType.STRING, owner.toString());
            display.setTransformation(transform(0.0F));
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(0);
        });
    }

    public void open(AnimationPair pair, int durationTicks) {
        if (pair == null || !pair.isValid()) return;
        pair.upper().setInterpolationDelay(0);
        pair.upper().setInterpolationDuration(durationTicks);
        pair.upper().setTransformation(transform(OPEN_ANGLE));
    }

    public void close(AnimationPair pair, int durationTicks) {
        if (pair == null || !pair.isValid()) return;
        pair.upper().setInterpolationDelay(0);
        pair.upper().setInterpolationDuration(durationTicks);
        pair.upper().setTransformation(transform(0.0F));
    }

    public void remove(AnimationPair pair) { if (pair != null) pair.remove(); }

    private static org.bukkit.util.Transformation transform(float angle) {
        return new org.bukkit.util.Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new AxisAngle4f(angle, 1.0F, 0.0F, 0.0F),
                new Vector3f(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F));
    }

    public record AnimationPair(ItemDisplay lower, ItemDisplay upper) {
        public boolean isValid() {
            return lower != null && upper != null && lower.isValid() && upper.isValid();
        }
        public void remove() {
            if (upper != null && upper.isValid()) upper.remove();
            if (lower != null && lower.isValid()) lower.remove();
        }
    }
}
