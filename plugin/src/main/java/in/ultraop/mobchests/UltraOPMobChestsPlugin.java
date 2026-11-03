package in.ultraop.mobchests;

import in.ultraop.mobchests.command.MobChestsCommand;
import in.ultraop.mobchests.config.MessageService;
import in.ultraop.mobchests.config.PluginSettings;
import in.ultraop.mobchests.listener.MobChestListener;
import in.ultraop.mobchests.fluid.LiquidGoldService;
import in.ultraop.mobchests.items.CustomItemsMenu;
import in.ultraop.mobchests.items.GoldenFoodListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

public final class UltraOPMobChestsPlugin extends JavaPlugin {
    private PluginSettings settings;
    private MessageService messages;
    private MobChestListener mobChestListener;
    private LiquidGoldService liquidGoldService;
    private CustomItemsMenu customItemsMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        settings = new PluginSettings(this);
        messages = new MessageService(this);
        settings.reload();
        messages.reload();

        customItemsMenu = new CustomItemsMenu(this);
        getServer().getPluginManager().registerEvents(customItemsMenu, this);
        getServer().getPluginManager().registerEvents(new GoldenFoodListener(this), this);

        MobChestsCommand executor = new MobChestsCommand(this, customItemsMenu);
        PluginCommand command = getCommand("mobchests");
        if (command == null) {
            getLogger().severe("Missing mobchests command in plugin.yml; disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        mobChestListener = new MobChestListener(this);
        liquidGoldService = new LiquidGoldService(this);
        getServer().getPluginManager().registerEvents(mobChestListener, this);
        getServer().getPluginManager().registerEvents(liquidGoldService, this);
        mobChestListener.recoverLoadedEntities();
        liquidGoldService.start();

        getLogger().info("Mob Chests enabled. Mob chest gameplay and Liquid Gold fluid simulation are active.");
    }

    @Override
    public void onDisable() {
        if (liquidGoldService != null) {
            liquidGoldService.shutdown();
            HandlerList.unregisterAll(liquidGoldService);
        }
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
