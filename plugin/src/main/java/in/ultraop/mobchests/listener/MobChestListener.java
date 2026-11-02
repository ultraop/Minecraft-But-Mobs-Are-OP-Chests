package in.ultraop.mobchests.listener;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.animation.ChestAnimation;
import in.ultraop.mobchests.animation.CowChestAnimator;
import in.ultraop.mobchests.animation.CreeperChestAnimator;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import in.ultraop.mobchests.loot.MobLootGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Two-click chest interaction for the Cow body-split and Creeper head-lid
 * prototypes. Other mobs remain untouched until their own models are ready.
 */
public final class MobChestListener implements Listener {
    private static final String NORMAL = "NORMAL";
    private static final String FROZEN = "FROZEN";
    private static final String OPENING = "OPENING";
    private static final String LOOT_OPEN = "LOOT_OPEN";
    private static final String CLOSING = "CLOSING";
    private static final String LOOTED = "LOOTED";

    private final UltraOPMobChestsPlugin plugin;
    private final CowChestAnimator cowAnimator;
    private final CreeperChestAnimator creeperAnimator;

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
    private final Map<UUID, ChestAnimation> animations = new HashMap<>();
    private final Map<UUID, Mob> activeMobs = new HashMap<>();
    private final Map<UUID, UUID> openers = new HashMap<>();

    public MobChestListener(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.cowAnimator = new CowChestAnimator(plugin);
        this.creeperAnimator = new CreeperChestAnimator(plugin);
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

    private boolean isSupported(Mob mob) {
        return mob instanceof Creeper || (mob instanceof Cow cow && cow.isAdult());
    }

    private boolean isLocked(Mob mob) {
        String state = getState(mob);
        return FROZEN.equals(state) || OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobRightClick(PlayerInteractEntityEvent event) {
        if (!plugin.settings().cowPrototypeEnabled()
                || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND
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
        // LOOTED mobs use their normal vanilla interaction and behaviour.
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
        mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.25F, 1.7F);
        player.sendMessage("§6§lMOB CHEST §8» §e" + mob.getType().name() + " frozen. Right-click again to open.");
    }

    private void applyFrozenState(Mob mob) {
        mob.setAI(false);
        mob.setAware(false);
        mob.setGravity(false);
        mob.setVelocity(new Vector(0.0, 0.0, 0.0));
        mob.setFallDistance(0.0F);
        mob.setTarget(null);
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
            } else {
                int interval = Math.max(2, plugin.getConfig().getInt("freeze.tremble-interval-ticks", 6));
                if (plugin.getConfig().getBoolean("freeze.subtle-tremble-enabled", true)
                        && ticks[0] % interval == 0) {
                    // Very light alternating sway: visible as a frightened tremble,
                    // but subtle enough not to become distracting.
                    float angle = (float) Math.max(0.0, Math.min(2.0,
                            plugin.getConfig().getDouble("freeze.tremble-angle-degrees", 0.65)));
                    float sway = ((ticks[0] / interval) % 2 == 0) ? angle : -angle;
                    mob.setRotation(anchor.getYaw() + sway, anchor.getPitch());
                }
            }
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
                yaw == null ? mob.getYaw() : yaw, pitch == null ? mob.getPitch() : pitch);
    }

    private ChestAnimation createAnimation(Mob mob) {
        if (mob instanceof Cow cow) return cowAnimator.create(cow);
        if (mob instanceof Creeper creeper) return creeperAnimator.create(creeper);
        throw new IllegalArgumentException("No visual animation model for " + mob.getType());
    }

    private void beginOpening(Mob mob, Player player) {
        UUID id = mob.getUniqueId();
        if (openers.containsKey(id)) return;

        stopTask(freezeTasks.remove(id));
        setState(mob, OPENING);
        applyFrozenState(mob);

        int openTicks = plugin.settings().openAnimationTicks();
        try {
            ChestAnimation animation = createAnimation(mob);
            animations.put(id, animation);
            mob.setInvisible(true);
            mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.9F, 1.0F);
            mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.65F, 1.15F);
            animation.open(openTicks);
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Mob chest animation could not start for " + id + ": " + exception.getMessage());
            recoverToFrozen(mob);
            player.sendMessage("§cThe chest animation could not start. The mob has been returned to its frozen state.");
            return;
        }

        activeMobs.put(id, mob);
        openers.put(id, player.getUniqueId());
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openTasks.remove(id);
            if (!mob.isValid() || mob.isDead() || !player.isOnline() || !OPENING.equals(getState(mob))) {
                consumeAndRestore(mob, false);
                return;
            }
            setState(mob, LOOT_OPEN);
            try {
                Inventory inventory = MobLootGenerator.create(id, mob.getType(), plugin);
                player.openInventory(inventory);
                launchLootBurst(mob, inventory);
                player.sendActionBar("Take what you need. This mob chest can only be opened once.");
            } catch (RuntimeException exception) {
                plugin.getLogger().severe("Could not open mob chest inventory for " + id + ": " + exception.getMessage());
                consumeAndRestore(mob, false);
                player.sendMessage("§cThe chest inventory failed to open. The mob has been safely restored.");
            }
        }, openTicks);
        openTasks.put(id, task);
    }

    private void launchLootBurst(Mob mob, Inventory inventory) {
        List<Integer> occupied = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && !stack.getType().isAir()) occupied.add(slot);
        }
        if (occupied.isEmpty()) return;

        Collections.shuffle(occupied, ThreadLocalRandom.current());
        int configuredBurst = Math.max(0, Math.min(4, plugin.getConfig().getInt("loot.burst-max-stacks", 2)));
        int burstStacks = Math.min(configuredBurst, Math.max(1, occupied.size() / 4));
        Location origin = mob.getLocation().clone().add(0.0, Math.max(0.9, mob.getHeight() * 0.7), 0.0);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < burstStacks; i++) {
            int slot = occupied.get(i);
            ItemStack stack = inventory.getItem(slot);
            if (stack == null || stack.getType().isAir()) continue;
            ItemStack ejected = stack.clone();
            inventory.clear(slot);

            double angle = random.nextDouble(0.0, Math.PI * 2.0);
            double horizontal = random.nextDouble(0.12, 0.32);
            double vertical = random.nextDouble(0.52, 0.76);
            Item item = mob.getWorld().dropItem(origin, ejected);
            item.setPickupDelay(10);
            item.setVelocity(new Vector(Math.cos(angle) * horizontal, vertical, Math.sin(angle) * horizontal));
        }

        mob.getWorld().playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8F, 1.0F);
        mob.getWorld().spawnParticle(Particle.CLOUD, origin, 14, 0.28, 0.24, 0.28, 0.04);
        mob.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, origin, 22, 0.32, 0.32, 0.32, 0.18);
        mob.getWorld().spawnParticle(Particle.CRIT, origin, 16, 0.25, 0.30, 0.25, 0.12);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MobChestInventoryHolder holder) {
            finishSession(holder.mobId(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
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

        setState(mob, CLOSING);
        openers.remove(mobId);
        stopTask(openTasks.remove(mobId));

        ChestAnimation animation = animations.get(mobId);
        int closeTicks = plugin.settings().closeAnimationTicks();
        if (animateClose && mob.isValid() && animation != null && animation.isValid()) {
            mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.9F, 1.0F);
            animation.close(closeTicks);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeAnimation(mobId);
                activeMobs.remove(mobId);
                setState(mob, LOOTED);
                restoreNormalState(mob);
            }, closeTicks);
        } else {
            removeAnimation(mobId);
            activeMobs.remove(mobId);
            setState(mob, LOOTED);
            restoreNormalState(mob);
        }
    }

    private void removeAnimation(UUID mobId) {
        ChestAnimation animation = animations.remove(mobId);
        if (animation != null) animation.remove();
    }

    private void consumeAndRestore(Mob mob, boolean animateClose) {
        finishSession(mob.getUniqueId(), animateClose);
        String state = getState(mob);
        if (OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state)) {
            setState(mob, LOOTED);
            removeAnimation(mob.getUniqueId());
            activeMobs.remove(mob.getUniqueId());
            openers.remove(mob.getUniqueId());
            restoreNormalState(mob);
        }
    }

    private void recoverToFrozen(Mob mob) {
        removeAnimation(mob.getUniqueId());
        activeMobs.remove(mob.getUniqueId());
        openers.remove(mob.getUniqueId());
        mob.setInvisible(readBool(mob, originalInvisibleKey, false));
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
                && isSupported(mob)
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSupportedMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !isSupportedType(mob)) return;
        UUID id = mob.getUniqueId();
        stopTask(freezeTasks.remove(id));
        stopTask(openTasks.remove(id));
        removeAnimation(id);
        activeMobs.remove(id);
        openers.remove(id);
        // Never change the mob's vanilla drops or experience.
    }

    private boolean isSupportedType(Mob mob) {
        return mob instanceof Cow || mob instanceof Creeper;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Mob mob) || !isSupportedType(mob)) continue;
            UUID id = mob.getUniqueId();
            stopTask(freezeTasks.remove(id));
            if (OPENING.equals(getState(mob)) || LOOT_OPEN.equals(getState(mob)) || CLOSING.equals(getState(mob))) {
                consumeAndRestore(mob, false);
            }
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
            if (!(entity instanceof Mob mob) || !isSupportedType(mob)) continue;
            String state = getState(mob);
            if (FROZEN.equals(state)) {
                applyFrozenState(mob);
                startFreezeEnforcer(mob);
            } else if (OPENING.equals(state) || LOOT_OPEN.equals(state) || CLOSING.equals(state)) {
                setState(mob, LOOTED);
                removeAnimation(mob.getUniqueId());
                restoreNormalState(mob);
            } else if (LOOTED.equals(state) && mob.isInvisible()) {
                restoreNormalState(mob);
            }
        }
    }

    public void shutdown() {
        for (BukkitTask task : freezeTasks.values()) task.cancel();
        freezeTasks.clear();

        for (UUID mobId : new ArrayList<>(activeMobs.keySet())) {
            Mob mob = activeMobs.get(mobId);
            if (mob != null && mob.isValid()) {
                setState(mob, LOOTED);
                removeAnimation(mobId);
                restoreNormalState(mob);
            }
        }
        activeMobs.clear();
        openers.clear();
        for (BukkitTask task : openTasks.values()) task.cancel();
        openTasks.clear();
        for (UUID id : new ArrayList<>(animations.keySet())) removeAnimation(id);
        // FROZEN mobs remain frozen in PDC and resume the enforcer on next enable.
    }
}
