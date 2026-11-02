package in.ultraop.mobchests.config;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginSettings {
    private final UltraOPMobChestsPlugin plugin;
    private FileConfiguration config;

    public PluginSettings(UltraOPMobChestsPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean debug() { return config.getBoolean("settings.debug", false); }
}
