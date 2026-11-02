package in.ultraop.mobchests.command;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MobChestsCommand implements CommandExecutor, TabCompleter {
    private final UltraOPMobChestsPlugin plugin;

    public MobChestsCommand(UltraOPMobChestsPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "info" -> showInfo(sender);
            case "help" -> showHelp(sender);
            case "status" -> showStatus(sender);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /mobchests help.");
        }
        return true;
    }

    private void showInfo(CommandSender sender) {
        sender.sendMessage(plugin.messages().color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(plugin.messages().color("&6&lULTRAOP MOB CHESTS"));
        sender.sendMessage(plugin.messages().color("&7Minecraft: &f26.3 &8| &7Server: &fPaper"));
        sender.sendMessage(plugin.messages().color("&7Plugin: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.messages().color("&7Required Java: &f25"));
        sender.sendMessage(plugin.messages().color("&7Components: &fPlugin + Datapack + Resource Pack"));
        sender.sendMessage(plugin.messages().color("&aImplemented prototype: &fAdult Cow freeze/open animation"));
        sender.sendMessage(plugin.messages().color("&aOne-time GUI: &fClose -> lid shuts -> Cow AI restored"));
        sender.sendMessage(plugin.messages().color("&ePrototype scope: &fCow only; custom Resource Pack required"));
        sender.sendMessage(plugin.messages().color("&ePlanned: &fall mobs, full loot tables, custom gear, Legend and bosses"));
        sender.sendMessage(plugin.messages().color("&8Use &e/mobchests help &8for commands."));
        sender.sendMessage(plugin.messages().color("&6&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void showHelp(CommandSender sender) {
        plugin.messages().sendLines(sender, "help.lines");
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage(plugin.messages().color("&6&lMOB CHESTS STATUS"));
        sender.sendMessage(plugin.messages().color("&7Plugin: &aEnabled"));
        sender.sendMessage(plugin.messages().color("&7Config: &aLoaded"));
        sender.sendMessage(plugin.messages().color("&7Cow interaction: &aPrototype enabled"));
        sender.sendMessage(plugin.messages().color("&7Cow chest open/close animation: &eRequires 26.3 resource pack"));
        sender.sendMessage(plugin.messages().color("&7All-mob loot/progression: &cNot implemented yet"));
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("ultraopmobchests.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }
        plugin.reloadPluginData();
        sender.sendMessage(plugin.messages().color("&aConfiguration reloaded."));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("info", "help", "status"));
            if (sender.hasPermission("ultraopmobchests.admin")) options.add("reload");
            return options.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
