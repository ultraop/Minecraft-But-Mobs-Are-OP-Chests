package in.ultraop.mobchests.config;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class MessageService {
    private final UltraOPMobChestsPlugin plugin;
    private YamlConfiguration messages;

    public MessageService(UltraOPMobChestsPlugin plugin) { this.plugin = plugin; }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "messages.yml"));
    }

    public void sendLines(CommandSender sender, String path) {
        for (String line : messages.getStringList(path)) {
            sender.sendMessage(color(line));
        }
    }

    public String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
