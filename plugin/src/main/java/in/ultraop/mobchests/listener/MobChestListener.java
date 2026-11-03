package in.ultraop.mobchests.listener;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import in.ultraop.mobchests.loot.MobLootGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resource-pack-free living mob chest interaction.
 *
 * The real entity remains visible and keeps its vanilla dimensions. AI is
 * disabled while the chest is active and vanilla entity pitch is applied
 * directly, preventing look goals from fighting the opening pose. Some mob
 * renderers may still interpret pitch differently; no model or scale is replaced.
 *
 * State: NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED.
 * Loot contents are saved on the mob itself and become its death drops after
 * the chest has been used. Vanilla item drops are replaced while normal XP remains.
 */
public final class MobChestListener implements Listener {
    private static final String NORMAL = "NORMAL";
    private static final String FROZEN = "FROZEN";
    private static final String OPENING = "OPENING";
    private static final String LOOT_OPEN = "LOOT_OPEN";
    private static final String CLOSING = "CLOSING";
    private static final String LOOTED = "LOOTED";

    private final UltraOPMobChestsPlugin plugin;

    private final NamespacedKey stateKey;
    private final NamespacedKey originalAiKey;
    private final NamespacedKey originalAwareKey;
    private final NamespacedKey originalGravityKey;
    private final NamespacedKey originalInvisibleKey;
    private final NamespacedKey originalCollidableKey;
    private final NamespacedKey anchorWorldKey;
    private final NamespacedKey anchorXKey;
    private final NamespacedKey anchorYKey;
    private final NamespacedKey anchorZKey;
    private final NamespacedKey anchorYawKey;
    private final NamespacedKey anchorPitchKey;
    private final NamespacedKey chestUsedKey;
    private final NamespacedKey chestLootKey;

    private final Map<UUID, BukkitTask> freezeTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> poseTasks = new HashMap<>();
    private final Map<UUID, Mob> activeMobs = new HashMap<>();
    private final Map<UUID, UUID> openers = new HashMap<>();
    private final Map<UUID, Inventory> activeInventories = new HashMap<>();
    private final Map<UUID, BukkitTask> offhandSuppressionTasks = new HashMap<>();

    public MobChestListener(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        stateKey = key("state");
        originalAiKey = key("original_ai");
        originalAwareKey = key("original_aware");
        originalGravityKey = key("original_gravity");
        originalInvisibleKey = key("original_invisible");
        originalCollidableKey = key("original_collidable");
        anchorWorldKey = key("anchor_world");
        anchorXKey = key("anchor_x");
        anchorYKey = key("anchor_y");
        anchorZKey = key("anchor_z");
        anchorYawKey = key("anchor_yaw");
        anchorPitchKey = key("anchor_pitch");
        chestUsedKey = key("chest_used");
        chestLootKey = key("chest_loot");
    }

    private NamespacedKey key(String value) {
        return new NamespacedKey(plugin, value);
    }

    private boolean isSupported(Mob mob) {
        return plugin.settings().interactionsEnabled(mob.getType());
    }

    private boolean isLocked(Mob mob) {
        String state = getState(mob);
        return FROZEN.equals(state) || OPENING.equals(state)
                || LOOT_OPEN.equals(state) || CLOSING.equals(state);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND
                || !(event.getRightClicked() instanceof Mob mob)
                || !isSupported(mob)) {
            return;
        }

        String state = getState(mob);
        if (NORMAL.equals(state)) {
            event.setCancelled(true);
            freeze(mob, event.getPlayer());
        } else if (FROZEN.equals(state)) {
            event.setCancelled(true);
            beginOpening(mob, event.getPlayer());
        } else if (OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state)) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar("This mob chest is already opening.");
        }
        // LOOTED mobs use their normal vanilla interaction again.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void handleSpecialRewardUse(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // PlayerInteractEvent can run once for each hand. Block the off-hand
        // armour's native equip/use action while a plugin enchantment book is used.
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            ItemStack mainhand = player.getInventory().getItemInMainHand();
            if (offhandSuppressionTasks.containsKey(playerId)
                    || isCustomEnchantmentBook(mainhand)
                    || isLiquidGoldBucket(offhand)) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType().isAir() || !held.hasItemMeta()) return;

        PersistentDataContainer itemData = held.getItemMeta().getPersistentDataContainer();
        String fluidId = itemData.get(
                new NamespacedKey(plugin, "custom_fluid_id"), PersistentDataType.STRING);
        if ("liquid_gold".equals(fluidId)) {
            event.setCancelled(true);
            suppressOffHandUse(player);
            player.sendActionBar("Liquid Gold is sealed in this build; it cannot place ordinary lava.");
            return;
        }

        String enchantmentId = itemData.get(
                new NamespacedKey(plugin, "custom_enchantment_id"), PersistentDataType.STRING);
        if (enchantmentId == null) return;

        // This also handles RIGHT_CLICK_AIR: Paper may mark that event as
        // cancelled when vanilla has no default action for the held book.
        event.setCancelled(true);
        suppressOffHandUse(player);

        Integer storedRank = itemData.get(
                new NamespacedKey(plugin, "custom_enchantment_rank"), PersistentDataType.INTEGER);
        int rank = storedRank == null ? 8 : Math.max(1, Math.min(15, storedRank));
        // Mending has no useful scaling beyond vanilla level I.
        if ("mending".equals(enchantmentId)) rank = 1;

        // Normalise legacy and current custom books on use. Custom ranks live
        // in PDC only; remove any old Vanilla StoredEnchantments and replace
        // the tooltip with the clamped level so enchantment.level.46 is gone.
        ItemMeta customBookMeta = held.getItemMeta();
        if (customBookMeta instanceof EnchantmentStorageMeta legacyMeta
                && !legacyMeta.getStoredEnchants().isEmpty()) {
            for (Enchantment oldEnchantment : new ArrayList<>(legacyMeta.getStoredEnchants().keySet())) {
                legacyMeta.removeStoredEnchant(oldEnchantment);
            }
        }
        customBookMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_enchantment_rank"), PersistentDataType.INTEGER, rank);
        customBookMeta.displayName(Component.text("Ancient Enchanted Book", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .decoration(TextDecoration.BOLD, TextDecoration.State.TRUE));
        customBookMeta.lore(List.of(
                Component.text("Enchantment: " + titleEnchantment(enchantmentId), NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Level: " + rank, NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Hold compatible gear in your off-hand.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                Component.text("Right-click while holding this book.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        held.setItemMeta(customBookMeta);
        player.getInventory().setItemInMainHand(held);

        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantmentId));
        if (enchantment == null) {
            player.sendMessage("§cThis enchantment is not available on this server version.");
            return;
        }

        ItemStack target = player.getInventory().getItemInOffHand();
        if (target == null || target.getType().isAir()) {
            player.sendMessage("§ePlace compatible armour or a tool in your off-hand first.");
            return;
        }
        if (!enchantment.canEnchantItem(target) || !supportsTarget(enchantmentId, target)) {
            player.sendMessage("§c" + titleEnchantment(enchantmentId)
                    + " requires compatible gear. Check the armour/tool type.");
            return;
        }

        int currentLevel = target.getEnchantmentLevel(enchantment);
        if (currentLevel >= rank) {
            player.sendMessage("§eThis item already has " + titleEnchantment(enchantmentId)
                    + " at level " + currentLevel + " or higher.");
            return;
        }

        for (Enchantment existing : target.getEnchantments().keySet()) {
            if (!existing.equals(enchantment)
                    && (enchantment.conflictsWith(existing) || existing.conflictsWith(enchantment))) {
                player.sendMessage("§c" + titleEnchantment(enchantmentId)
                        + " conflicts with " + existing.getKey().getKey().replace('_', ' ') + ".");
                return;
            }
        }

        ItemStack upgraded = target.clone();
        upgraded.addUnsafeEnchantment(enchantment, rank);
        player.getInventory().setItemInOffHand(upgraded);

        ItemStack book = player.getInventory().getItemInMainHand();
        if (book.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(org.bukkit.Material.AIR));
        } else {
            book.setAmount(book.getAmount() - 1);
            player.getInventory().setItemInMainHand(book);
        }

        player.sendMessage("§aApplied " + titleEnchantment(enchantmentId)
                + " " + rank + " to " + target.getType().name().toLowerCase().replace('_', ' ') + ".");
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7F, 1.35F);
    }

    private boolean supportsTarget(String enchantmentId, ItemStack item) {
        org.bukkit.Material type = item.getType();
        boolean helmet = type.name().endsWith("_HELMET") || type == org.bukkit.Material.TURTLE_HELMET;
        boolean boots = type.name().endsWith("_BOOTS");
        boolean leggings = type.name().endsWith("_LEGGINGS");
        boolean armour = helmet || boots || leggings || type.name().endsWith("_CHESTPLATE");
        boolean sword = type.name().endsWith("_SWORD") || type.name().endsWith("_AXE");
        boolean bow = type == org.bukkit.Material.BOW;
        boolean crossbow = type == org.bukkit.Material.CROSSBOW;
        boolean trident = type == org.bukkit.Material.TRIDENT;
        boolean tool = type.name().endsWith("_PICKAXE") || type.name().endsWith("_AXE")
                || type.name().endsWith("_SHOVEL") || type.name().endsWith("_HOE")
                || type == org.bukkit.Material.SHEARS;

        return switch (enchantmentId) {
            case "respiration" -> helmet;
            case "feather_falling", "depth_strider", "soul_speed" -> boots;
            case "swift_sneak" -> leggings;
            case "protection", "blast_protection", "fire_protection",
                    "projectile_protection", "thorns" -> armour;
            case "sharpness", "smite", "bane_of_arthropods",
                    "sweeping_edge", "fire_aspect", "knockback", "looting" -> sword;
            case "power", "flame", "infinity" -> bow;
            case "quick_charge", "piercing", "multishot" -> crossbow;
            case "fortune", "efficiency", "silk_touch" -> tool;
            case "impaling", "loyalty", "riptide" -> trident;
            case "unbreaking" -> item.getType().getMaxDurability() > 0;
            default -> enchantmentId.equals("mending") && item.getType().getMaxDurability() > 0;
        };
    }

    private boolean isCustomEnchantmentBook(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "custom_enchantment_id"), PersistentDataType.STRING);
    }

    private boolean isLiquidGoldBucket(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return "liquid_gold".equals(item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "custom_fluid_id"), PersistentDataType.STRING));
    }

    private String titleEnchantment(String key) {
        StringBuilder result = new StringBuilder();
        for (String word : key.split("_")) {
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private void suppressOffHandUse(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask previous = offhandSuppressionTasks.remove(id);
        if (previous != null) previous.cancel();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
                () -> offhandSuppressionTasks.remove(id), 2L);
        offhandSuppressionTasks.put(id, task);
    }

    private void freeze(Mob mob, Player player) {
        PersistentDataContainer data = mob.getPersistentDataContainer();
        data.set(originalAiKey, PersistentDataType.BYTE, bool(mob.hasAI()));
        data.set(originalAwareKey, PersistentDataType.BYTE, bool(mob.isAware()));
        data.set(originalGravityKey, PersistentDataType.BYTE, bool(mob.hasGravity()));
        data.set(originalInvisibleKey, PersistentDataType.BYTE, bool(mob.isInvisible()));
        data.set(originalCollidableKey, PersistentDataType.BYTE, bool(mob.isCollidable()));

        Location anchor = mob.getLocation();
        data.set(anchorWorldKey, PersistentDataType.STRING, anchor.getWorld().getUID().toString());
        data.set(anchorXKey, PersistentDataType.DOUBLE, anchor.getX());
        data.set(anchorYKey, PersistentDataType.DOUBLE, anchor.getY());
        data.set(anchorZKey, PersistentDataType.DOUBLE, anchor.getZ());
        data.set(anchorYawKey, PersistentDataType.FLOAT, anchor.getYaw());
        data.set(anchorPitchKey, PersistentDataType.FLOAT, anchor.getPitch());

        setState(mob, FROZEN);
        applyFrozenState(mob);
        startFreezeEnforcer(mob);
        mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.22F, 1.8F);
        player.sendMessage("§6§lMOB CHEST §8» §e" + mob.getType().name()
                + " frozen. Right-click again to open.");
    }

    private void applyFrozenState(Mob mob) {
        // Stop all vanilla look/target goals so they cannot fight the chest pose.
        // The chest pose is applied directly with setRotation while AI is disabled.
        mob.setAI(false);
        mob.setAware(false);
        mob.setGravity(false);
        mob.setVelocity(new Vector(0.0, 0.0, 0.0));
        mob.setFallDistance(0.0F);
        mob.setTarget(null);
        mob.getPathfinder().stopPathfinding();
        mob.setAggressive(false);
        if (mob instanceof Creeper creeper) {
            disarmCreeper(creeper);
        }
    }

    private void disarmCreeper(Creeper creeper) {
        creeper.setFuseTicks(0);
        creeper.setIgnited(false);
    }

    private void startFreezeEnforcer(Mob mob) {
        UUID id = mob.getUniqueId();
        stopTask(freezeTasks.remove(id));
        int[] ticks = {0};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !FROZEN.equals(getState(mob))) {
                stopTask(freezeTasks.remove(id));
                return;
            }

            applyFrozenState(mob);
            Location anchor = readAnchor(mob);
            if (anchor == null) return;

            if (!mob.getWorld().equals(anchor.getWorld())
                    || mob.getLocation().distanceSquared(anchor) > 0.0001) {
                mob.teleport(anchor);
            }

            int interval = Math.max(2,
                    plugin.getConfig().getInt("freeze.tremble-interval-ticks", 8));
            float amplitude = (float) Math.max(0.0, Math.min(2.0,
                    plugin.getConfig().getDouble("freeze.tremble-head-degrees", 0.8)));
            float tremble = 0.0F;
            if (plugin.getConfig().getBoolean("freeze.subtle-tremble-enabled", true)) {
                tremble = ticks[0] % interval < Math.max(1, interval / 2)
                        ? amplitude : -amplitude;
            }
            applyHeadPitch(mob, anchor, anchor.getPitch() + tremble);
            ticks[0]++;
        }, 1L, 1L);
        freezeTasks.put(id, task);
    }

    private Location readAnchor(Mob mob) {
        PersistentDataContainer data = mob.getPersistentDataContainer();
        String worldId = data.get(anchorWorldKey, PersistentDataType.STRING);
        Double x = data.get(anchorXKey, PersistentDataType.DOUBLE);
        Double y = data.get(anchorYKey, PersistentDataType.DOUBLE);
        Double z = data.get(anchorZKey, PersistentDataType.DOUBLE);
        Float yaw = data.get(anchorYawKey, PersistentDataType.FLOAT);
        Float pitch = data.get(anchorPitchKey, PersistentDataType.FLOAT);
        if (worldId == null || x == null || y == null || z == null) return null;

        org.bukkit.World world;
        try {
            world = Bukkit.getWorld(UUID.fromString(worldId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return world == null ? null : new Location(world, x, y, z,
                yaw == null ? mob.getYaw() : yaw,
                pitch == null ? mob.getPitch() : pitch);
    }

    /**
     * Apply a fixed vanilla entity pitch with AI disabled. This avoids the
     * Mob look goals alternately replacing the target and causing a head
     * animation to flicker up/down. Body yaw and the entity dimensions remain.
     */
    private void applyHeadPitch(Mob mob, Location anchor, float pitch) {
        float clampedPitch = Math.max(-90.0F, Math.min(90.0F, pitch));
        mob.setRotation(anchor.getYaw(), clampedPitch);
        mob.setBodyYaw(anchor.getYaw());
    }

    private void beginOpening(Mob mob, Player player) {
        UUID id = mob.getUniqueId();
        if (openers.containsKey(id)) return;

        stopTask(freezeTasks.remove(id));
        setState(mob, OPENING);
        applyFrozenState(mob);

        Location anchor = readAnchor(mob);
        if (anchor == null) {
            recoverToFrozen(mob);
            player.sendMessage("§cCould not read the mob's saved position. Try again.");
            return;
        }
        if (!mob.getWorld().equals(anchor.getWorld())
                || mob.getLocation().distanceSquared(anchor) > 0.0001) {
            mob.teleport(anchor);
        }

        Inventory inventory;
        try {
            inventory = MobLootGenerator.create(id, mob.getType(),
                    mob.getWorld().getEnvironment(), plugin);
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Could not generate mob chest loot for " + id + ": " + exception.getMessage());
            recoverToFrozen(mob);
            player.sendMessage("§cThe chest could not prepare its loot. The mob remains frozen.");
            return;
        }

        activeMobs.put(id, mob);
        activeInventories.put(id, inventory);
        openers.put(id, player.getUniqueId());
        mob.getPersistentDataContainer().set(chestUsedKey, PersistentDataType.BYTE, (byte) 1);
        persistInventory(mob, inventory);

        Location effectLocation = mob.getLocation().clone().add(0.0, Math.max(0.7, mob.getHeight() * 0.7), 0.0);
        mob.getWorld().playSound(effectLocation, Sound.BLOCK_CHEST_OPEN, 0.9F, 1.0F);
        mob.getWorld().playSound(effectLocation, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.55F, 1.2F);
        mob.getWorld().spawnParticle(Particle.END_ROD, effectLocation, 8, 0.12, 0.12, 0.12, 0.025);

        animateHeadPitch(mob, anchor.getPitch(), plugin.settings().openingHeadPitchDegrees(),
                plugin.settings().openAnimationTicks(), OPENING, () -> {
                    if (!mob.isValid() || mob.isDead()) return;
                    if (!player.isOnline()) {
                        persistInventory(mob, inventory);
                        setState(mob, LOOTED);
                        activeMobs.remove(id);
                        activeInventories.remove(id);
                        openers.remove(id);
                        restoreNormalState(mob);
                        return;
                    }

                    setState(mob, LOOT_OPEN);
                    startOpenPoseHold(mob, plugin.settings().openingHeadPitchDegrees());
                    launchLootBurst(mob, inventory);
                    persistInventory(mob, inventory);
                    try {
                        player.openInventory(inventory);
                        player.sendActionBar("Take what you need. Closing this inventory uses the chest permanently.");
                    } catch (RuntimeException exception) {
                        plugin.getLogger().severe("Could not open mob chest GUI for " + id + ": " + exception.getMessage());
                        persistInventory(mob, inventory);
                        finishSession(id, false);
                        player.sendMessage("§cThe chest GUI failed to open. Remaining loot is saved on the mob.");
                    }
                });
    }

    private void animateHeadPitch(Mob mob, float startPitch, float endPitch,
                                  int durationTicks, String expectedState, Runnable completed) {
        UUID id = mob.getUniqueId();
        stopTask(poseTasks.remove(id));
        int duration = Math.max(1, durationTicks);
        int[] elapsed = {0};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !expectedState.equals(getState(mob))) {
                stopTask(poseTasks.remove(id));
                return;
            }
            Location anchor = readAnchor(mob);
            if (anchor == null) {
                stopTask(poseTasks.remove(id));
                setState(mob, LOOTED);
                restoreNormalState(mob);
                return;
            }

            applyFrozenState(mob);
            if (!mob.getWorld().equals(anchor.getWorld())
                    || mob.getLocation().distanceSquared(anchor) > 0.0001) {
                mob.teleport(anchor);
            }

            elapsed[0]++;
            float progress = Math.min(1.0F, elapsed[0] / (float) duration);
            float eased = progress * progress * (3.0F - 2.0F * progress);
            float pitch = startPitch + (endPitch - startPitch) * eased;
            applyHeadPitch(mob, anchor, pitch);

            if (elapsed[0] >= duration) {
                stopTask(poseTasks.remove(id));
                completed.run();
            }
        }, 1L, 1L);
        poseTasks.put(id, task);
    }

    /**
     * Keep the Mob anchored with its head held open for the entire GUI session.
     * The old freeze task was removed at the beginning of the opening animation,
     * so this hold task owns the mob until the inventory closes.
     */
    private void startOpenPoseHold(Mob mob, float openPitch) {
        UUID id = mob.getUniqueId();
        stopTask(freezeTasks.remove(id));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !LOOT_OPEN.equals(getState(mob))) {
                stopTask(freezeTasks.remove(id));
                return;
            }
            Location anchor = readAnchor(mob);
            if (anchor == null) return;
            applyFrozenState(mob);
            if (!mob.getWorld().equals(anchor.getWorld())
                    || mob.getLocation().distanceSquared(anchor) > 0.0001) {
                mob.teleport(anchor);
            }
            applyHeadPitch(mob, anchor, openPitch);
        }, 1L, 1L);
        freezeTasks.put(id, task);
    }

    private void launchLootBurst(Mob mob, Inventory inventory) {
        List<Integer> occupied = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && !stack.getType().isAir()) occupied.add(slot);
        }
        if (occupied.isEmpty()) return;

        Collections.shuffle(occupied, ThreadLocalRandom.current());
        int configuredBurst = Math.max(0,
                Math.min(36, plugin.getConfig().getInt("loot.burst-max-stacks", 18)));
        double burstRatio = Math.max(0.0, Math.min(1.0,
                plugin.getConfig().getDouble("loot.burst-ratio", 0.50)));
        int halfBurst = (int) Math.ceil(occupied.size() * burstRatio);
        int burstStacks = Math.min(occupied.size(), Math.min(configuredBurst, halfBurst));
        Location origin = mob.getLocation().clone()
                .add(0.0, Math.max(0.9, mob.getHeight() * 0.75), 0.0);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < burstStacks; i++) {
            int slot = occupied.get(i);
            ItemStack stack = inventory.getItem(slot);
            if (stack == null || stack.getType().isAir()) continue;

            // Remove from the GUI before spawning a real collectible item.
            ItemStack ejected = stack.clone();
            inventory.clear(slot);
            Location spawnAt = origin.clone().add(
                    random.nextDouble(-0.16, 0.16),
                    random.nextDouble(-0.08, 0.12),
                    random.nextDouble(-0.16, 0.16));
            Item item = mob.getWorld().dropItem(spawnAt, ejected);
            item.setPickupDelay(14);

            double angle = random.nextDouble(0.0, Math.PI * 2.0);
            // Low horizontal speed keeps the items within roughly 3–4 blocks.
            double horizontal = random.nextDouble(0.10, 0.15);
            double vertical = random.nextDouble(0.52, 0.62);
            item.setVelocity(new Vector(
                    Math.cos(angle) * horizontal,
                    vertical,
                    Math.sin(angle) * horizontal));
        }

        mob.getWorld().playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.75F, 1.05F);
        mob.getWorld().spawnParticle(Particle.CLOUD, origin, 22, 0.36, 0.30, 0.36, 0.055);
        mob.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, origin, 30, 0.42, 0.36, 0.42, 0.18);
        mob.getWorld().spawnParticle(Particle.CRIT, origin, 24, 0.38, 0.32, 0.38, 0.16);
        mob.getWorld().spawnParticle(Particle.END_ROD, origin, 16, 0.40, 0.38, 0.40, 0.12);
    }

    private void persistInventory(Mob mob, Inventory inventory) {
        if (mob == null || !mob.isValid() || inventory == null) return;
        byte[] contents = ItemStack.serializeItemsAsBytes(inventory.getContents());
        PersistentDataContainer data = mob.getPersistentDataContainer();
        data.set(chestUsedKey, PersistentDataType.BYTE, (byte) 1);
        data.set(chestLootKey, PersistentDataType.BYTE_ARRAY, contents);
    }

    private void persistOpenInventory(Inventory inventory) {
        if (!(inventory.getHolder() instanceof MobChestInventoryHolder holder)) return;
        Mob mob = activeMobs.get(holder.mobId());
        if (mob != null && LOOT_OPEN.equals(getState(mob))) {
            persistInventory(mob, inventory);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        persistTopInventoryNextTick(event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        persistTopInventoryNextTick(event.getView().getTopInventory());
    }

    private void persistTopInventoryNextTick(Inventory inventory) {
        if (!(inventory.getHolder() instanceof MobChestInventoryHolder)) return;
        Bukkit.getScheduler().runTask(plugin, () -> persistOpenInventory(inventory));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MobChestInventoryHolder holder)) return;
        Mob mob = activeMobs.get(holder.mobId());
        if (mob != null) persistInventory(mob, event.getInventory());
        finishSession(holder.mobId(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Inventory top = event.getPlayer().getOpenInventory().getTopInventory();
        persistOpenInventory(top);

        UUID playerId = event.getPlayer().getUniqueId();
        BukkitTask suppression = offhandSuppressionTasks.remove(playerId);
        if (suppression != null) suppression.cancel();
        UUID mobId = openers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(playerId))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (mobId != null) finishSession(mobId, true);
    }

    private void finishSession(UUID mobId, boolean animateClose) {
        Mob mob = activeMobs.get(mobId);
        if (mob == null) return;
        String state = getState(mob);
        if (!OPENING.equals(state) && !LOOT_OPEN.equals(state)) return;

        Inventory inventory = activeInventories.get(mobId);
        if (inventory != null) persistInventory(mob, inventory);

        setState(mob, CLOSING);
        openers.remove(mobId);
        stopTask(freezeTasks.remove(mobId));
        stopTask(poseTasks.remove(mobId));

        Location anchor = readAnchor(mob);
        if (animateClose && mob.isValid() && anchor != null) {
            mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.85F, 1.0F);
            animateHeadPitch(mob, plugin.settings().openingHeadPitchDegrees(), anchor.getPitch(),
                    plugin.settings().closeAnimationTicks(), CLOSING, () -> {
                        activeInventories.remove(mobId);
                        activeMobs.remove(mobId);
                        setState(mob, LOOTED);
                        restoreNormalState(mob);
                    });
        } else {
            activeInventories.remove(mobId);
            activeMobs.remove(mobId);
            setState(mob, LOOTED);
            restoreNormalState(mob);
        }
    }

    private void recoverToFrozen(Mob mob) {
        UUID id = mob.getUniqueId();
        stopTask(poseTasks.remove(id));
        activeInventories.remove(id);
        activeMobs.remove(id);
        openers.remove(id);
        setState(mob, FROZEN);
        applyFrozenState(mob);
        startFreezeEnforcer(mob);
    }

    private void restoreNormalState(Mob mob) {
        if (mob == null || !mob.isValid() || mob.isDead()) return;
        PersistentDataContainer data = mob.getPersistentDataContainer();

        mob.setAI(readBool(mob, originalAiKey, true));
        mob.setAware(readBool(mob, originalAwareKey, true));
        mob.setGravity(readBool(mob, originalGravityKey, true));
        mob.setInvisible(readBool(mob, originalInvisibleKey, false));
        mob.setCollidable(readBool(mob, originalCollidableKey, true));
        mob.setVelocity(new Vector(0.0, 0.0, 0.0));
        mob.setFallDistance(0.0F);

        Location anchor = readAnchor(mob);
        if (anchor != null && anchor.getWorld().equals(mob.getWorld())) {
            mob.setRotation(anchor.getYaw(), anchor.getPitch());
            mob.setBodyYaw(anchor.getYaw());
        }
        if (mob instanceof Creeper creeper) disarmCreeper(creeper);

        removeStoredOriginals(data);
        removeAnchor(data);
    }

    private boolean readBool(Mob mob, NamespacedKey key, boolean fallback) {
        Byte value = mob.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return value == null ? fallback : value != 0;
    }

    private void removeStoredOriginals(PersistentDataContainer data) {
        data.remove(originalAiKey);
        data.remove(originalAwareKey);
        data.remove(originalGravityKey);
        data.remove(originalInvisibleKey);
        data.remove(originalCollidableKey);
    }

    private void removeAnchor(PersistentDataContainer data) {
        data.remove(anchorWorldKey);
        data.remove(anchorXKey);
        data.remove(anchorYKey);
        data.remove(anchorZKey);
        data.remove(anchorYawKey);
        data.remove(anchorPitchKey);
    }

    private static byte bool(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    private String getState(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(
                stateKey, PersistentDataType.STRING, NORMAL);
    }

    private void setState(Mob mob, String state) {
        mob.getPersistentDataContainer().set(stateKey, PersistentDataType.STRING, state);
    }

    private void stopTask(BukkitTask task) {
        if (task != null) task.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectOpeningMob(EntityDamageEvent event) {
        if (event.getEntity() instanceof Mob mob
                && (OPENING.equals(getState(mob)) || LOOT_OPEN.equals(getState(mob))
                || CLOSING.equals(getState(mob)))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void stopFrozenCreeperExplosion(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof Creeper creeper && isLocked(creeper)) {
            event.setCancelled(true);
            disarmCreeper(creeper);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        UUID id = mob.getUniqueId();
        stopTask(freezeTasks.remove(id));
        stopTask(poseTasks.remove(id));
        activeMobs.remove(id);
        activeInventories.remove(id);
        openers.remove(id);

        Byte used = mob.getPersistentDataContainer().get(chestUsedKey, PersistentDataType.BYTE);
        if (used == null || used == 0) {
            // The chest was never opened: ordinary vanilla mob loot remains.
            return;
        }

        // Once the chest has been used, it replaces rather than duplicates
        // the mob's native item drops.
        event.getDrops().clear();
        // Keep normal vanilla experience; only item drops are replaced.

        byte[] stored = mob.getPersistentDataContainer().get(
                chestLootKey, PersistentDataType.BYTE_ARRAY);
        if (stored == null || stored.length == 0) return;

        try {
            ItemStack[] remaining = ItemStack.deserializeItemsFromBytes(stored);
            for (ItemStack stack : remaining) {
                if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) continue;
                Item drop = mob.getWorld().dropItemNaturally(mob.getLocation(), stack.clone());
                drop.setPickupDelay(10);
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Could not restore saved chest drops for " + id + ": " + exception.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Mob mob)) continue;
            UUID id = mob.getUniqueId();
            String state = getState(mob);

            if (FROZEN.equals(state)) {
                stopTask(freezeTasks.remove(id));
                continue; // The saved FROZEN state resumes when the chunk loads.
            }

            if (OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state)) {
                Inventory inventory = activeInventories.get(id);
                if (inventory != null) persistInventory(mob, inventory);
                else persistPlayerOpenInventory(id);

                setState(mob, CLOSING);
                UUID openerId = openers.get(id);
                Player opener = openerId == null ? null : Bukkit.getPlayer(openerId);
                if (opener != null) opener.closeInventory();

                stopTask(poseTasks.remove(id));
                stopTask(freezeTasks.remove(id));
                activeInventories.remove(id);
                activeMobs.remove(id);
                openers.remove(id);
                setState(mob, LOOTED);
                restoreNormalState(mob);
            }
        }
    }

    private void persistPlayerOpenInventory(UUID mobId) {
        UUID playerId = openers.get(mobId);
        Player player = playerId == null ? null : Bukkit.getPlayer(playerId);
        if (player == null) return;
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top.getHolder() instanceof MobChestInventoryHolder holder
                && holder.mobId().equals(mobId)) {
            Mob mob = activeMobs.get(mobId);
            if (mob != null) persistInventory(mob, top);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        recoverChunk(event.getChunk());
    }

    public void recoverLoadedEntities() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) recoverChunk(chunk);
        }
    }

    private void recoverChunk(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Mob mob)) continue;
            String state = getState(mob);
            if (FROZEN.equals(state)) {
                applyFrozenState(mob);
                startFreezeEnforcer(mob);
            } else if (OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state)) {
                // Interrupted chest sessions remain consumed. The last saved
                // contents are kept on the entity and drop when it dies.
                stopTask(poseTasks.remove(mob.getUniqueId()));
                activeInventories.remove(mob.getUniqueId());
                activeMobs.remove(mob.getUniqueId());
                openers.remove(mob.getUniqueId());
                setState(mob, LOOTED);
                restoreNormalState(mob);
            } else if (LOOTED.equals(state) && mob.isInvisible()) {
                // Compatibility recovery for entities opened by older builds.
                restoreNormalState(mob);
            }
        }
    }

    public void shutdown() {
        for (BukkitTask task : freezeTasks.values()) task.cancel();
        freezeTasks.clear();
        for (BukkitTask task : poseTasks.values()) task.cancel();
        poseTasks.clear();
        for (BukkitTask task : offhandSuppressionTasks.values()) task.cancel();
        offhandSuppressionTasks.clear();

        for (UUID mobId : new ArrayList<>(activeMobs.keySet())) {
            Mob mob = activeMobs.get(mobId);
            if (mob != null && mob.isValid()) {
                Inventory inventory = activeInventories.get(mobId);
                if (inventory != null) persistInventory(mob, inventory);
                else persistPlayerOpenInventory(mobId);
                setState(mob, LOOTED);
                restoreNormalState(mob);
            }
        }
        activeInventories.clear();
        activeMobs.clear();
        openers.clear();
        // Frozen mobs remain frozen in PDC and resume on the next plugin enable.
    }
}
