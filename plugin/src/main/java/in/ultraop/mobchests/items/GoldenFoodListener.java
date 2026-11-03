package in.ultraop.mobchests.items;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies one food-specific random bonus for a random 5–10 seconds.
 * The edible component itself is retained from the vanilla food item.
 */
public final class GoldenFoodListener implements Listener {
    private record Power(PotionEffectType type, int minimumAmplifier, int maximumAmplifier) { }

    private static final Map<String, List<Power>> POWERS = Map.ofEntries(
            Map.entry("golden_potato", List.of(
                    new Power(PotionEffectType.SPEED, 0, 1),
                    new Power(PotionEffectType.JUMP_BOOST, 0, 1))),
            Map.entry("golden_baked_potato", List.of(
                    new Power(PotionEffectType.HASTE, 0, 1),
                    new Power(PotionEffectType.RESISTANCE, 0, 0))),
            Map.entry("golden_steak", List.of(
                    new Power(PotionEffectType.STRENGTH, 0, 1),
                    new Power(PotionEffectType.ABSORPTION, 0, 1))),
            Map.entry("golden_porkchop", List.of(
                    new Power(PotionEffectType.REGENERATION, 0, 1),
                    new Power(PotionEffectType.RESISTANCE, 0, 0))),
            Map.entry("golden_chicken", List.of(
                    new Power(PotionEffectType.SPEED, 0, 1),
                    new Power(PotionEffectType.NIGHT_VISION, 0, 0))),
            Map.entry("golden_mutton", List.of(
                    new Power(PotionEffectType.RESISTANCE, 0, 1),
                    new Power(PotionEffectType.STRENGTH, 0, 0))),
            Map.entry("golden_rabbit", List.of(
                    new Power(PotionEffectType.JUMP_BOOST, 1, 2),
                    new Power(PotionEffectType.SLOW_FALLING, 0, 0))),
            Map.entry("golden_bread", List.of(
                    new Power(PotionEffectType.HASTE, 0, 1),
                    new Power(PotionEffectType.SPEED, 0, 0))),
            Map.entry("golden_carrot", List.of(
                    new Power(PotionEffectType.NIGHT_VISION, 0, 0),
                    new Power(PotionEffectType.LUCK, 0, 1))),
            Map.entry("golden_cod", List.of(
                    new Power(PotionEffectType.WATER_BREATHING, 0, 0),
                    new Power(PotionEffectType.DOLPHINS_GRACE, 0, 1))),
            Map.entry("golden_salmon", List.of(
                    new Power(PotionEffectType.CONDUIT_POWER, 0, 0),
                    new Power(PotionEffectType.DOLPHINS_GRACE, 0, 1))),
            Map.entry("golden_pumpkin_pie", List.of(
                    new Power(PotionEffectType.FIRE_RESISTANCE, 0, 0),
                    new Power(PotionEffectType.REGENERATION, 0, 1)))
    );

    private final UltraOPMobChestsPlugin plugin;
    private final NamespacedKey customItemIdKey;

    public GoldenFoodListener(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.customItemIdKey = new NamespacedKey(plugin, "custom_item_id");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGoldenFoodConsume(PlayerItemConsumeEvent event) {
        ItemStack consumed = event.getItem();
        if (consumed == null || !consumed.hasItemMeta()) return;

        String id = consumed.getItemMeta().getPersistentDataContainer()
                .get(customItemIdKey, PersistentDataType.STRING);
        List<Power> options = POWERS.get(id);
        if (options == null || options.isEmpty()) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Power selected = options.get(random.nextInt(options.size()));
        int durationTicks = random.nextInt(100, 201); // 5–10 seconds
        int amplifier = random.nextInt(selected.minimumAmplifier(),
                selected.maximumAmplifier() + 1);
        Player player = event.getPlayer();

        // Apply after the vanilla consume finishes so it cannot be overwritten
        // by the base food's consume routine.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            player.addPotionEffect(new PotionEffect(
                    selected.type(), durationTicks, amplifier, false, false, true), true);
            player.sendActionBar("§6Golden food bonus: §e"
                    + selected.type().getKey().getKey().replace('_', ' ')
                    + " §7(" + (durationTicks / 20.0) + "s)");
        });
    }
}
