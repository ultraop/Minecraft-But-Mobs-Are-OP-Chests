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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
 * State: NORMAL -> FROZEN <-> OPENING -> LOOT_OPEN -> CLOSING. Mob chests remain reusable until the mob dies.
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
        // Reusable mob chests return to FROZEN after each close.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
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

        final Inventory inventory;
        try {
            Inventory savedInventory = loadSavedInventory(mob);
            inventory = savedInventory != null ? savedInventory
                    : MobLootGenerator.create(id, mob.getType(), mob.getWorld().getEnvironment(), plugin);
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Could not prepare mob chest loot for " + id + ": " + exception.getMessage());
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
        // Scale the opening sound only slightly by Mob size. Small animals stay
        // quiet, while larger mobs and bosses sound a little fuller.
        float sizeFactor = (float) Math.max(0.0, Math.min(1.0, (mob.getHeight() - 0.65) / 2.8));
        float chestVolume = 0.72F + 0.18F * sizeFactor;
        float rocketVolume = 0.16F + 0.06F * sizeFactor;
        mob.getWorld().playSound(effectLocation, Sound.BLOCK_CHEST_OPEN, chestVolume, 1.0F);
        mob.getWorld().playSound(effectLocation, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, rocketVolume, 1.2F);
        mob.getWorld().spawnParticle(Particle.END_ROD, effectLocation, 8, 0.12, 0.12, 0.12, 0.025);

        animateHeadPitch(mob, anchor.getPitch(), plugin.settings().openingHeadPitchDegrees(),
                plugin.settings().openAnimationTicks(), OPENING, () -> {
                    if (!mob.isValid() || mob.isDead()) return;
                    if (!player.isOnline()) {
                        persistInventory(mob, inventory);
                        restoreNormalState(mob);
                        setState(mob, NORMAL);
                        activeMobs.remove(id);
                        activeInventories.remove(id);
                        openers.remove(id);
                        return;
                    }

                    setState(mob, LOOT_OPEN);
                    startOpenPoseHold(mob, plugin.settings().openingHeadPitchDegrees());
                    persistInventory(mob, inventory);
                    try {
                        player.openInventory(inventory);
                        player.sendActionBar("Take what you need. Close the inventory and right-click the mob again to reopen this chest.");
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
                Inventory inventory = activeInventories.get(id);
                if (inventory != null) persistInventory(mob, inventory);
                activeInventories.remove(id);
                activeMobs.remove(id);
                openers.remove(id);
                restoreNormalState(mob);
                setState(mob, NORMAL);
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

    private Inventory loadSavedInventory(Mob mob) {
        byte[] stored = mob.getPersistentDataContainer().get(chestLootKey, PersistentDataType.BYTE_ARRAY);
        if (stored == null || stored.length == 0) return null;
        ItemStack[] contents = ItemStack.deserializeItemsFromBytes(stored);
        Inventory inventory = Bukkit.createInventory(
                new MobChestInventoryHolder(mob.getUniqueId()), 54,
                Component.text(mob.getType().name().replace('_', ' ') + " Mob Chest",
                        NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        int count = Math.min(contents.length, inventory.getSize());
        for (int i = 0; i < count; i++) {
            ItemStack stack = contents[i];
            if (stack != null && !stack.getType().isAir() && stack.getAmount() > 0) {
                inventory.setItem(i, stack);
            }
        }
        return inventory;
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
                        restoreNormalState(mob);
                        setState(mob, NORMAL);
                    });
        } else {
            activeInventories.remove(mobId);
            activeMobs.remove(mobId);
            restoreNormalState(mob);
            setState(mob, NORMAL);
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
                if (opener != null) {
                    Inventory top = opener.getOpenInventory().getTopInventory();
                    if (top.getHolder() instanceof MobChestInventoryHolder holder
                            && holder.mobId().equals(id)) {
                        opener.closeInventory();
                    }
                }

                stopTask(poseTasks.remove(id));
                stopTask(freezeTasks.remove(id));
                activeInventories.remove(id);
                activeMobs.remove(id);
                openers.remove(id);
                setState(mob, FROZEN);
                applyFrozenState(mob);
                startFreezeEnforcer(mob);
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
                // Interrupted chest sessions return to the frozen, reusable state.
                // Saved remaining contents are available on the next opening.
                stopTask(poseTasks.remove(mob.getUniqueId()));
                activeInventories.remove(mob.getUniqueId());
                activeMobs.remove(mob.getUniqueId());
                openers.remove(mob.getUniqueId());
                setState(mob, FROZEN);
                applyFrozenState(mob);
                startFreezeEnforcer(mob);
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

        for (UUID mobId : new ArrayList<>(activeMobs.keySet())) {
            Mob mob = activeMobs.get(mobId);
            if (mob != null && mob.isValid()) {
                Inventory inventory = activeInventories.get(mobId);
                if (inventory != null) persistInventory(mob, inventory);
                else persistPlayerOpenInventory(mobId);

                // Close a live player's GUI while this listener is still
                // registered, so no temporary inventory remains interactable
                // after a plugin reload/disable.
                setState(mob, CLOSING);
                UUID openerId = openers.get(mobId);
                Player opener = openerId == null ? null : Bukkit.getPlayer(openerId);
                if (opener != null) {
                    Inventory top = opener.getOpenInventory().getTopInventory();
                    if (top.getHolder() instanceof MobChestInventoryHolder holder
                            && holder.mobId().equals(mobId)) {
                        opener.closeInventory();
                    }
                }

                Inventory current = activeInventories.get(mobId);
                if (current != null) persistInventory(mob, current);
                activeInventories.remove(mobId);
                activeMobs.remove(mobId);
                openers.remove(mobId);
                setState(mob, FROZEN);
                applyFrozenState(mob);
                startFreezeEnforcer(mob);
            }
        }
        activeInventories.clear();
        activeMobs.clear();
        openers.clear();
        // Opened mob chests return to the reusable FROZEN state and resume on the next plugin enable.
    }
}
