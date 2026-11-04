package in.ultraop.mobchests.command;
import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.ChatColor; import org.bukkit.command.*; import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;
import java.util.List; import java.util.Locale;
public final class MobChestsCommand implements CommandExecutor,TabCompleter {
 private final UltraOPMobChestsPlugin plugin;
 public MobChestsCommand(UltraOPMobChestsPlugin plugin){this.plugin=plugin;}
 @Override public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label,@NotNull String[] args){
  if(args.length==0){showHelp(sender);return true;}
  switch(args[0].toLowerCase(Locale.ROOT)){case "info"->showInfo(sender);case "help"->showHelp(sender);case "status"->showStatus(sender);case "reload"->reload(sender);default->sender.sendMessage(ChatColor.RED+"Unknown subcommand. Use /mobchests help.");}return true;
 }
 private void showInfo(CommandSender sender){
  sender.sendMessage(plugin.messages().color("&6&lMOB CHESTS — ABOUT"));sender.sendMessage(plugin.messages().color("&7Minecraft: &f26.3 &8| &7Server: &fPaper"));
  sender.sendMessage(plugin.messages().color("&7Plugin: &f"+plugin.getDescription().getVersion()));sender.sendMessage(plugin.messages().color("&7Required Java: &f25"));
  sender.sendMessage(plugin.messages().color("&7Created by: &fUltra OP"));sender.sendMessage(plugin.messages().color("&aLoot: &fMob-strength-scaled vanilla rewards"));
 }
 private void showHelp(CommandSender sender){plugin.messages().sendLines(sender,"help.lines");}
 private void showStatus(CommandSender sender){
  sender.sendMessage(plugin.messages().color("&6&lMOB CHESTS STATUS"));sender.sendMessage(plugin.messages().color("&7Plugin: &aEnabled"));
  sender.sendMessage(plugin.messages().color("&7Config: &aLoaded"));sender.sendMessage(plugin.messages().color("&7Mob support: &a"+plugin.settings().supportedMobTypeCount()+" / "+plugin.settings().totalMobTypeCount()));
  sender.sendMessage(plugin.messages().color("&7Mob chest interaction: &aEnabled"));sender.sendMessage(plugin.messages().color("&7Tiered vanilla loot: &aEnabled"));
  sender.sendMessage(plugin.messages().color("&7All custom items/resource pack: &cRemoved"));
 }
 private void reload(CommandSender sender){if(!sender.hasPermission("mobchests.admin")){sender.sendMessage(ChatColor.RED+"You do not have permission.");return;}plugin.reloadPluginData();sender.sendMessage(plugin.messages().color("&aConfiguration reloaded."));}
 @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,@NotNull Command command,@NotNull String alias,@NotNull String[] args){
  if(args.length==1){List<String> options=sender.hasPermission("mobchests.admin")?List.of("info","help","status","reload"):List.of("info","help","status");return options.stream().filter(s->s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();}return List.of();
 }
}
