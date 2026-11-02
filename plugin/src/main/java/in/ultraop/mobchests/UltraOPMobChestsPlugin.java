package in.ultraop.mobchests;

import in.ultraop.mobchests.command.MobChestsCommand;
import in.ultraop.mobchests.config.MessageService;
import in.ultraop.mobchests.config.PluginSettings;
import in.ultraop.mobchests.listener.MobChestListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

public final class UltraOPMobChestsPlugin extends JavaPlugin {
    private PluginSettings settings;
    private MessageService messages;
    private MobChestListener mobChestListener;

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

        mobChestListener = new MobChestListener(this);
        getServer().getPluginManager().registerEvents(mobChestListener, this);
        mobChestListener.recoverLoadedEntities();

        getLogger().info("Mob Chests enabled. Resource-pack-free vanilla mob pose interactions and persistent chest loot are active.");
    }

    @Override
    public void onDisable() {
        if (mobChestListener != null) {
            mobChestListener.shutdown();
            HandlerList.unregisterAll(mobChestListener);
        }
        getLogger().info("Mob Chests disabled.");
    }

    public void reloadPluginData() {
        reloadConfig();
        settings.reload();
        messages.reload();
    }

    public PluginSettings settings() { return settings; }
    public MessageService messages() { return messages; }
}
