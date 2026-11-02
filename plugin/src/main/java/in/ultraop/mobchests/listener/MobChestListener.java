package in.ultraop.mobchests.listener;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import in.ultraop.mobchests.inventory.MobChestInventoryHolder;
import in.ultraop.mobchests.loot.MobLootGenerator;
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
import org.bukkit.event.player.PlayerInteractEvent;
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
 * The real entity remains visible and keeps its vanilla dimensions. Paper's
 * entity pitch is used for the lid-like head/front lift, while body yaw is
 * held at its original value. Mob-specific renderers may interpret pitch
 * differently; no client model or scale replacement is performed.
 *
 * State: NORMAL -> FROZEN -> OPENING -> LOOT_OPEN -> CLOSING -> LOOTED.
 * Loot contents are saved on the mob itself and become its death drops after
 * the chest has been used. Vanilla drops and XP are then suppressed.
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
        // LOOTED mobs use their normal vanilla interaction again.
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
        // Keep the entity's look controller available for Mob.lookAt(),
        // while awareness=false prevents ordinary autonomous behaviour.
        mob.setAI(true);
        mob.setAware(false);
        mob.setGravity(false);
        mob.setVelocity(new Vector(0.0, 0.0, 0.0));
        mob.setFallDistance(0.0F);
        mob.setTarget(null);
        mob.getPathfinder().stopPathfinding();
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
                    plugin.getConfig().getDouble("freeze.tremble-pitch-degrees", 0.8)));
            float tremble = 0.0F;
            if (plugin.getConfig().getBoolean("freeze.subtle-tremble-enabled", true)) {
                tremble = ticks[0] % interval < Math.max(1, interval / 2)
                        ? amplitude : -amplitude;
            }
            applyHeadLookPose(mob, anchor, tremble);
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
     * Use Paper's Mob look controller rather than rotating the whole entity.
     * The real vanilla model and bounding box remain untouched. A target
     * almost directly above the eye line raises the head/front to its maximum
     * vanilla head pitch. This is a head-look pose, not a custom model hinge.
     */
    private void applyHeadLookPose(Mob mob, Location anchor, float lookUpDegrees) {
        float angle = Math.max(-2.0F, Math.min(88.0F, lookUpDegrees));
        double yawRadians = Math.toRadians(anchor.getYaw());
        double distance = 4.0;
        double radians = Math.toRadians(angle);
        double horizontal = Math.max(0.025, distance * Math.cos(radians));
        double vertical = distance * Math.sin(radians);
        double dx = -Math.sin(yawRadians) * horizontal;
        double dz = Math.cos(yawRadians) * horizontal;

        Location target = new Location(anchor.getWorld(),
                anchor.getX() + dx,
                anchor.getY() + mob.getEyeHeight() + vertical,
                anchor.getZ() + dz);

        mob.setBodyYaw(anchor.getYaw());
        mob.lookAt(target, 360.0F, 90.0F);
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

        animateHeadLook(mob, 0.0F, 88.0F,
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

    private void animateHeadLook(Mob mob, float startAngle, float endAngle,
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

            elapsed[0]++;
            float progress = Math.min(1.0F, elapsed[0] / (float) duration);
            float eased = progress * progress * (3.0F - 2.0F * progress);
            float lookUp = startAngle + (endAngle - startAngle) * eased;
            applyHeadLookPose(mob, anchor, lookUp);

            if (elapsed[0] >= duration) {
                stopTask(poseTasks.remove(id));
                completed.run();
            }
        }, 1L, 1L);
        poseTasks.put(id, task);
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
                Math.min(4, plugin.getConfig().getInt("loot.burst-max-stacks", 2)));
        int burstStacks = Math.min(configuredBurst, occupied.size());
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
            Item item = mob.getWorld().dropItem(origin, ejected);
            item.setPickupDelay(12);

            double angle = random.nextDouble(0.0, Math.PI * 2.0);
            double horizontal = random.nextDouble(0.12, 0.30);
            double vertical = random.nextDouble(0.48, 0.72);
            item.setVelocity(new Vector(
                    Math.cos(angle) * horizontal,
                    vertical,
                    Math.sin(angle) * horizontal));
        }

        mob.getWorld().playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.75F, 1.05F);
        mob.getWorld().spawnParticle(Particle.CLOUD, origin, 10, 0.24, 0.18, 0.24, 0.035);
        mob.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, origin, 15, 0.26, 0.25, 0.26, 0.12);
        mob.getWorld().spawnParticle(Particle.CRIT, origin, 12, 0.20, 0.22, 0.20, 0.10);
        mob.getWorld().spawnParticle(Particle.END_ROD, origin, 8, 0.22, 0.22, 0.22, 0.08);
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
            animateHeadLook(mob, 88.0F, 0.0F,
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
            applyHeadLookPose(mob, anchor, 0.0F);
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
        // the mob's native drops and XP.
        event.getDrops().clear();
        event.setDroppedExp(0);

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
