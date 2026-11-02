package in.ultraop.mobchests;

import in.ultraop.mobchests.command.MobChestsCommand;
import in.ultraop.mobchests.config.MessageService;
import in.ultraop.mobchests.config.PluginSettings;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraOPMobChestsPlugin extends JavaPlugin {
    private PluginSettings settings;
    private MessageService messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        settings = new PluginSettings(this);
        messages = new MessageService(this);
        settings.reload();
        messages.reload();

        MobChestsCommand executor = new MobChestsCommand(this);
        PluginCommand command = getCommand("mobchests");
        if (command == null) {
            getLogger().severe("Missing mobchests command in plugin.yml; disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        getLogger().info("Enabled initial scaffold. Gameplay modules are not implemented yet.");
    }

    @Override
    public void onDisable() {
        getLogger().info("UltraOP Mob Chests disabled.");
    }

    public void reloadPluginData() {
        reloadConfig();
        settings.reload();
        messages.reload();
    }

    public PluginSettings settings() { return settings; }
    public MessageService messages() { return messages; }
}
