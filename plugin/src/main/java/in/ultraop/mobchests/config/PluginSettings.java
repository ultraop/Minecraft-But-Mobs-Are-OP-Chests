package in.ultraop.mobchests.config;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

public final class PluginSettings {
    private final UltraOPMobChestsPlugin plugin;
    private FileConfiguration config;

    public PluginSettings(UltraOPMobChestsPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean interactionsEnabled(EntityType type) {
        if (!config.getBoolean("prototype.enabled", true)) return false;
        if (config.getStringList("prototype.excluded-entities").stream()
                .anyMatch(name -> name.equalsIgnoreCase(type.name()))) return false;
        if (config.getBoolean("prototype.support-all-mobs", true)) return true;
        return config.getStringList("prototype.supported-entities").stream()
                .anyMatch(name -> name.equalsIgnoreCase(type.name()));
    }

    public int totalMobTypeCount() {
        int count = 0;
        for (EntityType type : EntityType.values()) {
            Class<? extends Entity> entityClass = type.getEntityClass();
            if (entityClass != null && Mob.class.isAssignableFrom(entityClass)) count++;
        }
        return count;
    }

    public int supportedMobTypeCount() {
        int count = 0;
        for (EntityType type : EntityType.values()) {
            Class<? extends Entity> entityClass = type.getEntityClass();
            if (entityClass != null && Mob.class.isAssignableFrom(entityClass)
                    && interactionsEnabled(type)) count++;
        }
        return count;
    }

    public float openingHeadPitchDegrees() {
        return -Math.max(0.0F, Math.min(88.0F,
                (float) Math.abs(config.getDouble("prototype.opening-head-look-degrees", 88.0))));
    }

    public float openingHeadLookDegrees() {
        return Math.abs(openingHeadPitchDegrees());
    }

    public int openAnimationTicks() {
        return clamp(config.getInt("prototype.open-animation-ticks", 12), 1, 60);
    }

    public int closeAnimationTicks() {
        return clamp(config.getInt("prototype.close-animation-ticks", 8), 1, 60);
    }

    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
