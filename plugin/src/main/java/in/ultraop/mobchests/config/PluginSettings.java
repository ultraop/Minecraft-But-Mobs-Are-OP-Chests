package in.ultraop.mobchests.config;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public final class PluginSettings {
    private final UltraOPMobChestsPlugin plugin;
    private FileConfiguration config;

    public PluginSettings(UltraOPMobChestsPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean debug() { return config.getBoolean("settings.debug", false); }

    public boolean interactionsEnabled(EntityType type) {
        return config.getBoolean("prototype.enabled", true)
                && config.getStringList("prototype.supported-entities").stream()
                .anyMatch(name -> name.equalsIgnoreCase(type.name()));
    }

    public int openAnimationTicks() {
        return clamp(config.getInt("prototype.open-animation-ticks", 10), 1, 60);
    }

    public int closeAnimationTicks() {
        return clamp(config.getInt("prototype.close-animation-ticks", 8), 1, 60);
    }

    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
