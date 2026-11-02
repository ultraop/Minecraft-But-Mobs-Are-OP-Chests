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
        sender.sendMessage(plugin.messages().color("&6&lMOB CHESTS — ABOUT"));
        sender.sendMessage(plugin.messages().color("&7Minecraft: &f26.3 &8| &7Server: &fPaper"));
        sender.sendMessage(plugin.messages().color("&7Plugin: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.messages().color("&7Required Java: &f25"));
        sender.sendMessage(plugin.messages().color("&7Created by: &fUltra OP"));
        sender.sendMessage(plugin.messages().color("&7Components: &fPaper Plugin only"));
        sender.sendMessage(plugin.messages().color("&aMob interaction: &fAll configured vanilla Mobs; no size/model replacement"));
        sender.sendMessage(plugin.messages().color("&aVisual pose: &fVanilla head-look control; no Resource Pack or Client Mod"));
        sender.sendMessage(plugin.messages().color("&aFreeze: &fAnchored with a subtle head tremble"));
        sender.sendMessage(plugin.messages().color("&aLoot opening: &fHead/front lift, firework burst and one GUI session"));
        sender.sendMessage(plugin.messages().color("&aDeath drops: &fRemaining chest items replace vanilla mob drops after use"));
        sender.sendMessage(plugin.messages().color("&aLoot: &fMob-family and dimension-aware progression pools"));
        sender.sendMessage(plugin.messages().color("&eLimits: &fIndependent tentacle/body-part animation and custom textures require a client renderer or Resource Pack"));
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
        sender.sendMessage(plugin.messages().color("&7Mob support: &aAll configured vanilla Mob entities"));
        sender.sendMessage(plugin.messages().color("&7Vanilla size/model: &aUnchanged"));
        sender.sendMessage(plugin.messages().color("&7Resource Pack / Client Mod: &aNot required"));
        sender.sendMessage(plugin.messages().color("&7Head/front pose: &aVanilla head-look animation"));
        sender.sendMessage(plugin.messages().color("&7Saved chest drops replace vanilla drops after use: &aEnabled"));
        sender.sendMessage(plugin.messages().color("&7Dimension loot: &aOverworld / Nether / End pools"));
        sender.sendMessage(plugin.messages().color("&7Liquid Gold real fluid and 10-second powers: &cNot implemented"));
        sender.sendMessage(plugin.messages().color("&7Custom body-part renderers / squid tentacles: &cNot supported by this no-pack mode"));
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
