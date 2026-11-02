package in.ultraop.mobchests.listener;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.animation.CowChestAnimator;
import in.ultraop.mobchests.animation.CowChestAnimator.AnimationPair;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import in.ultraop.mobchests.loot.CowLootGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * First visual/gameplay vertical slice: one-time Cow chest interaction.
 *
 * State transitions:
 * NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> LOOTED
 *
 * The original Cow is never replaced. Its AI/physics state is saved and
 * restored. During the open session it is hidden behind two temporary
 * resource-pack-backed ItemDisplays. All state changes occur on the server
 * thread and are persisted in the Cow's PDC.
 */
public final class MobChestListener implements Listener {
    private static final String NORMAL = "NORMAL";
    private static final String FROZEN = "FROZEN";
    private static final String OPENING = "OPENING";
    private static final String LOOT_OPEN = "LOOT_OPEN";
    private static final String LOOTED = "LOOTED";

    private final UltraOPMobChestsPlugin plugin;
    private final CowChestAnimator animator;

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

    private final Map<UUID, BukkitTask> freezeTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> openTasks = new HashMap<>();
    private final Map<UUID, AnimationPair> animations = new HashMap<>();
    private final Map<UUID, Cow> activeMobs = new HashMap<>();
    private final Map<UUID, UUID> openers = new HashMap<>();

    public MobChestListener(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.animator = new CowChestAnimator(plugin);
        this.stateKey = key("state");
        this.originalAiKey = key("original_ai");
        this.originalAwareKey = key("original_aware");
        this.originalGravityKey = key("original_gravity");
        this.originalInvisibleKey = key("original_invisible");
        this.originalCollidableKey = key("original_collidable");
        this.anchorWorldKey = key("anchor_world");
        this.anchorXKey = key("anchor_x");
        this.anchorYKey = key("anchor_y");
        this.anchorZKey = key("anchor_z");
        this.anchorYawKey = key("anchor_yaw");
        this.anchorPitchKey = key("anchor_pitch");
    }

    private NamespacedKey key(String value) {
        return new NamespacedKey(plugin, value);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobRightClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        if (!(event.getRightClicked() instanceof Cow cow)) {
            return; // The first animation proof-of-concept is deliberately Cow-only.
        }

        String state = getState(cow);
        if (NORMAL.equals(state)) {
            event.setCancelled(true);
            freeze(cow, event.getPlayer());
            return;
        }
        if (FROZEN.equals(state)) {
            event.setCancelled(true);
            beginOpening(cow, event.getPlayer());
            return;
        }
        if (OPENING.equals(state) || LOOT_OPEN.equals(state)) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar("This mob chest is already opening.");
        }
        // LOOTED is intentionally not cancelled: the Cow behaves like a
        // normal Cow again, but the plugin never opens its chest a second time.
    }

    private void freeze(Cow cow, Player player) {
        PersistentDataContainer data = cow.getPersistentDataContainer();
        data.set(originalAiKey, PersistentDataType.BYTE, bool(cow.hasAI()));
        data.set(originalAwareKey, PersistentDataType.BYTE, bool(cow.isAware()));
        data.set(originalGravityKey, PersistentDataType.BYTE, bool(cow.hasGravity()));
        data.set(originalInvisibleKey, PersistentDataType.BYTE, bool(cow.isInvisible()));
        data.set(originalCollidableKey, PersistentDataType.BYTE, bool(cow.isCollidable()));

        Location anchor = cow.getLocation();
        data.set(anchorWorldKey, PersistentDataType.STRING, anchor.getWorld().getUID().toString());
        data.set(anchorXKey, PersistentDataType.DOUBLE, anchor.getX());
        data.set(anchorYKey, PersistentDataType.DOUBLE, anchor.getY());
        data.set(anchorZKey, PersistentDataType.DOUBLE, anchor.getZ());
        data.set(anchorYawKey, PersistentDataType.FLOAT, anchor.getYaw());
        data.set(anchorPitchKey, PersistentDataType.FLOAT, anchor.getPitch());
        setState(cow, FROZEN);
        applyFrozenState(cow);
        startFreezeEnforcer(cow);

        player.sendMessage("§6§lMOB CHEST §8» §eCow frozen. Right-click it again to open.");
    }

    private void applyFrozenState(Mob mob) {
        mob.setAI(false);
        mob.setAware(false);
        mob.setGravity(false);
        mob.setCollidable(false);
        mob.setVelocity(new Vector(0.0, 0.0, 0.0));
        mob.setFallDistance(0.0F);
        mob.setTarget(null);
    }

    private void startFreezeEnforcer(Mob mob) {
        UUID id = mob.getUniqueId();
        stopTask(freezeTasks.remove(id));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!mob.isValid() || mob.isDead() || !FROZEN.equals(getState(mob))) {
                stopTask(freezeTasks.remove(id));
                return;
            }
            applyFrozenState(mob);
            Location anchor = readAnchor(mob);
            if (anchor == null) {
                return;
            }
            if (!mob.getWorld().equals(anchor.getWorld())
                    || mob.getLocation().distanceSquared(anchor) > 0.0001
                    || Math.abs(mob.getYaw() - anchor.getYaw()) > 1.0F
                    || Math.abs(mob.getPitch() - anchor.getPitch()) > 1.0F) {
                mob.teleport(anchor);
            }
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
        if (worldId == null || x == null || y == null || z == null) {
            return null;
        }
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

    private void beginOpening(Cow cow, Player player) {
        UUID id = cow.getUniqueId();
        if (openers.containsKey(id)) {
            return;
        }

        stopTask(freezeTasks.remove(id));
        setState(cow, OPENING);
        applyFrozenState(cow);

        AnimationPair pair;
        try {
            pair = animator.create(cow);
            animations.put(id, pair);
            cow.setInvisible(true);
            cow.getWorld().playSound(cow.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.9F, 1.0F);
            animator.open(pair);
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Cow chest animation could not start for " + id + ": " + exception.getMessage());
            recoverToFrozen(cow);
            player.sendMessage("§cThe mob chest animation could not start. The Cow has been returned to its frozen state.");
            return;
        }

        activeMobs.put(id, cow);
        openers.put(id, player.getUniqueId());
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openTasks.remove(id);
            if (!cow.isValid() || cow.isDead() || !player.isOnline() || !OPENING.equals(getState(cow))) {
                consumeAndRestore(cow, false);
                return;
            }
            setState(cow, LOOT_OPEN);
            try {
                Inventory inventory = CowLootGenerator.create(id);
                player.openInventory(inventory);
                player.sendActionBar("Take what you need. Closing this inventory uses this mob chest permanently.");
            } catch (RuntimeException exception) {
                plugin.getLogger().severe("Could not open Cow chest inventory for " + id + ": " + exception.getMessage());
                consumeAndRestore(cow, false);
                player.sendMessage("§cThe chest inventory failed to open. The Cow has been safely restored.");
            }
        }, 10L);
        openTasks.put(id, task);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MobChestInventoryHolder holder)) {
            return;
        }
        finishSession(holder.mobId(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        UUID mobId = openers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(playerId))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (mobId != null) {
            finishSession(mobId, true);
        }
    }

    private void finishSession(UUID mobId, boolean animateClose) {
        Cow cow = activeMobs.get(mobId);
        if (cow == null) {
            return;
        }
        String state = getState(cow);
        if (!OPENING.equals(state) && !LOOT_OPEN.equals(state)) {
            return;
        }

        // Mark used before starting the close animation: repeated clicks or
        // duplicate close events cannot create a second inventory session.
        setState(cow, LOOTED);
        openers.remove(mobId);
        stopTask(openTasks.remove(mobId));

        AnimationPair pair = animations.get(mobId);
        if (animateClose && cow.isValid() && pair != null && pair.isValid()) {
            cow.getWorld().playSound(cow.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.9F, 1.0F);
            animator.close(pair);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                animator.remove(animations.remove(mobId));
                activeMobs.remove(mobId);
                restoreNormalState(cow);
            }, 8L);
        } else {
            animator.remove(animations.remove(mobId));
            activeMobs.remove(mobId);
            restoreNormalState(cow);
        }
    }

    private void consumeAndRestore(Cow cow, boolean animateClose) {
        finishSession(cow.getUniqueId(), animateClose);
        if (OPENING.equals(getState(cow)) || LOOT_OPEN.equals(getState(cow))) {
            setState(cow, LOOTED);
            animator.remove(animations.remove(cow.getUniqueId()));
            activeMobs.remove(cow.getUniqueId());
            openers.remove(cow.getUniqueId());
            restoreNormalState(cow);
        }
    }

    private void recoverToFrozen(Cow cow) {
        animator.remove(animations.remove(cow.getUniqueId()));
        activeMobs.remove(cow.getUniqueId());
        openers.remove(cow.getUniqueId());
        cow.setInvisible(readBool(cow, originalInvisibleKey, false));
        setState(cow, FROZEN);
        applyFrozenState(cow);
        startFreezeEnforcer(cow);
    }

    private void restoreNormalState(Cow cow) {
        if (cow == null || !cow.isValid() || cow.isDead()) {
            return;
        }
        PersistentDataContainer data = cow.getPersistentDataContainer();
        cow.setAI(readBool(cow, originalAiKey, true));
        cow.setAware(readBool(cow, originalAwareKey, true));
        cow.setGravity(readBool(cow, originalGravityKey, true));
        cow.setInvisible(readBool(cow, originalInvisibleKey, false));
        cow.setCollidable(readBool(cow, originalCollidableKey, true));
        cow.setVelocity(new Vector(0.0, 0.0, 0.0));
        cow.setFallDistance(0.0F);

        removeStoredOriginals(data);
        removeAnchor(data);
    }

    private boolean readBool(Cow cow, NamespacedKey key, boolean fallback) {
        Byte value = cow.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
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

    private String getState(Cow cow) {
        return cow.getPersistentDataContainer().getOrDefault(
                stateKey, PersistentDataType.STRING, NORMAL);
    }

    private void setState(Cow cow, String state) {
        cow.getPersistentDataContainer().set(stateKey, PersistentDataType.STRING, state);
    }

    private void stopTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectOpeningCow(EntityDamageEvent event) {
        if (event.getEntity() instanceof Cow cow) {
            String state = getState(cow);
            if (OPENING.equals(state) || LOOT_OPEN.equals(state)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCowDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Cow cow)) {
            return;
        }
        UUID id = cow.getUniqueId();
        stopTask(freezeTasks.remove(id));
        stopTask(openTasks.remove(id));
        animator.remove(animations.remove(id));
        activeMobs.remove(id);
        openers.remove(id);
        // Do not modify drops or XP: vanilla death rewards must remain intact.
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Cow cow)) {
                continue;
            }
            UUID id = cow.getUniqueId();
            stopTask(freezeTasks.remove(id));
            if (OPENING.equals(getState(cow)) || LOOT_OPEN.equals(getState(cow))) {
                consumeAndRestore(cow, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        recoverChunk(event.getChunk());
    }

    public void recoverLoadedEntities() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                recoverChunk(chunk);
            }
        }
    }

    private void recoverChunk(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Cow cow)) {
                continue;
            }
            String state = getState(cow);
            if (FROZEN.equals(state)) {
                applyFrozenState(cow);
                startFreezeEnforcer(cow);
            } else if (OPENING.equals(state) || LOOT_OPEN.equals(state)) {
                // A server crash/restart cannot leave a hidden or permanently
                // half-open Cow. The one-time attempt is consumed safely.
                setState(cow, LOOTED);
                animator.remove(animations.remove(cow.getUniqueId()));
                restoreNormalState(cow);
            } else if (LOOTED.equals(state) && cow.isInvisible()) {
                // Defensive recovery if the server stopped between state
                // transition and visibility restoration.
                restoreNormalState(cow);
            }
        }
    }

    public void shutdown() {
        for (BukkitTask task : freezeTasks.values()) {
            task.cancel();
        }
        freezeTasks.clear();

        for (UUID mobId : new java.util.ArrayList<>(activeMobs.keySet())) {
            Cow cow = activeMobs.get(mobId);
            if (cow != null && cow.isValid()) {
                setState(cow, LOOTED);
                animator.remove(animations.remove(mobId));
                restoreNormalState(cow);
            }
        }
        activeMobs.clear();
        openers.clear();
        for (BukkitTask task : openTasks.values()) {
            task.cancel();
        }
        openTasks.clear();
        for (AnimationPair pair : animations.values()) {
            animator.remove(pair);
        }
        animations.clear();
        // FROZEN mobs deliberately remain frozen; their PDC state lets the
        // next plugin enable reattach the freeze-enforcer task.
    }
}
