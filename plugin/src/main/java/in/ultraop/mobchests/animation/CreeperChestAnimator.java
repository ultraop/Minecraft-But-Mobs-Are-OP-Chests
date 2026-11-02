package in.ultraop.mobchests.animation;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Creeper-specific head-lid animation. The lower body and four legs remain
 * fixed while the square head rotates backward from the neck seam.
 */
public final class CreeperChestAnimator {
    private static final String RESOURCE_NAMESPACE = "ultraop_mobchests";
    private static final float MODEL_SCALE = 1.45F;
    private static final float OPEN_ANGLE = (float) Math.toRadians(-96.0F);

    private final UltraOPMobChestsPlugin plugin;
    private final NamespacedKey ownerKey;

    public CreeperChestAnimator(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "animation_owner");
    }

    public AnimationPair create(Creeper creeper) {
        // The model's y=8 line is placed slightly below the Creeper's neck.
        Location seam = creeper.getLocation().clone().add(0.0, 0.67, 0.0);
        UUID owner = creeper.getUniqueId();
        ItemDisplay lower = spawnPart(seam, "creeper_base", owner, creeper.getYaw());
        ItemDisplay upper = null;
        try {
            upper = spawnPart(seam, "creeper_head", owner, creeper.getYaw());
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
        meta.setItemModel(new NamespacedKey(RESOURCE_NAMESPACE, model));
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

    private static org.bukkit.util.Transformation transform(float angle) {
        // Pivot compensation keeps the rear-bottom edge of the head at the
        // neck seam while the display interpolates its rotation.
        float hingeOffsetZ = -4.0F / 16.0F * MODEL_SCALE;
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        Vector3f translation = new Vector3f(
                0.0F,
                -hingeOffsetZ * sin,
                hingeOffsetZ * (cos - 1.0F));
        return new org.bukkit.util.Transformation(
                translation,
                new AxisAngle4f(angle, 1.0F, 0.0F, 0.0F),
                new Vector3f(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F));
    }

    public record AnimationPair(ItemDisplay lower, ItemDisplay upper) implements ChestAnimation {
        @Override
        public boolean isValid() {
            return lower != null && upper != null && lower.isValid() && upper.isValid();
        }

        @Override
        public void open(int durationTicks) {
            if (!isValid()) return;
            upper.setInterpolationDelay(0);
            upper.setInterpolationDuration(durationTicks);
            upper.setTransformation(transform(OPEN_ANGLE));
        }

        @Override
        public void close(int durationTicks) {
            if (!isValid()) return;
            upper.setInterpolationDelay(0);
            upper.setInterpolationDuration(durationTicks);
            upper.setTransformation(transform(0.0F));
        }

        @Override
        public void remove() {
            if (upper != null && upper.isValid()) upper.remove();
            if (lower != null && lower.isValid()) lower.remove();
        }
    }
}
