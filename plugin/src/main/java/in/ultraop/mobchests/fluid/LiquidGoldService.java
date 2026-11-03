package in.ultraop.mobchests.fluid;

import in.ultraop.mobchests.UltraOPMobChestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Water-physics-backed Liquid Gold simulation.
 *
 * Vanilla WATER supplies swimming, collision, flow timing and infinite sources.
 * The plugin tracks its cells and adds persistent ItemDisplay overlays using a
 * companion 26.3 resource pack to show a translucent golden surface without
 * changing normal water or lava textures.
 */
public final class LiquidGoldService implements Listener {
    private static final BlockFace[] HORIZONTAL = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final UltraOPMobChestsPlugin plugin;
    private final NamespacedKey displayMarkerKey;
    private final NamespacedKey displaySourceKey;
    private final NamespacedKey displayLevelKey;
    private final NamespacedKey displayXKey;
    private final NamespacedKey displayYKey;
    private final NamespacedKey displayZKey;

    private final Map<FluidKey, FluidCell> cells = new HashMap<>();
    private final Set<UUID> insidePlayers = new HashSet<>();
    private final Map<UUID, Long> lastPowerAt = new HashMap<>();
    private final Map<UUID, BukkitTask> drinkTasks = new HashMap<>();

    private BukkitTask flowTask;

    public LiquidGoldService(UltraOPMobChestsPlugin plugin) {
        this.plugin = plugin;
        this.displayMarkerKey = new NamespacedKey(plugin, "liquid_gold_cell");
        this.displaySourceKey = new NamespacedKey(plugin, "liquid_gold_source");
        this.displayLevelKey = new NamespacedKey(plugin, "liquid_gold_level");
        this.displayXKey = new NamespacedKey(plugin, "liquid_gold_x");
        this.displayYKey = new NamespacedKey(plugin, "liquid_gold_y");
        this.displayZKey = new NamespacedKey(plugin, "liquid_gold_z");
    }

    public void start() {
        scanLoadedChunks();
        // Native WATER ticks own all spreading and source regeneration.
        // This task only refreshes immersion effects for stationary players.
        flowTask = Bukkit.getScheduler().runTaskTimer(
                plugin, () -> {
                    refreshTrackedCells();
                    refreshImmersionEffects();
                }, 10L, 10L);
    }

    public void shutdown() {
        if (flowTask != null) {
            flowTask.cancel();
            flowTask = null;
        }
        // Do not remove the water blocks or persistent display entities. Their
        // source/level metadata is stored on each display and is restored on
        // plugin enable or chunk load.
        for (BukkitTask task : drinkTasks.values()) task.cancel();
        drinkTasks.clear();
        cells.clear();
        insidePlayers.clear();
        lastPowerAt.clear();
    }

    public static boolean isLiquidGoldBucket(ItemStack item, UltraOPMobChestsPlugin plugin) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "custom_fluid_id"), PersistentDataType.STRING);
        return "liquid_gold".equals(id);
    }

    public static ItemStack createBucket(UltraOPMobChestsPlugin plugin) {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(new NamespacedKey(plugin, "liquid_gold_bucket"));
        meta.setMaxStackSize(1);
        meta.displayName(net.kyori.adventure.text.Component.text("Liquid Gold Bucket",
                net.kyori.adventure.text.format.NamedTextColor.GOLD)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC,
                        net.kyori.adventure.text.format.TextDecoration.State.FALSE)
                .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD,
                        net.kyori.adventure.text.format.TextDecoration.State.TRUE));
        meta.lore(List.of(
                net.kyori.adventure.text.Component.text("Golden water-like fluid",
                        net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC,
                                net.kyori.adventure.text.format.TextDecoration.State.FALSE),
                net.kyori.adventure.text.Component.text("Place on a block to pour; right-click air to drink.",
                        net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC,
                                net.kyori.adventure.text.format.TextDecoration.State.FALSE),
                net.kyori.adventure.text.Component.text("Collect a source with an empty bucket.",
                        net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC,
                                net.kyori.adventure.text.format.TextDecoration.State.FALSE)));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_fluid_id"),
                PersistentDataType.STRING, "liquid_gold");
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBucketUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            if (event.getHand() == EquipmentSlot.OFF_HAND
                    && isLiquidGoldBucket(event.getItem(), plugin)) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (isLiquidGoldBucket(held, plugin)) {
            // RIGHT_CLICK_AIR is commonly pre-cancelled by Paper because a
            // vanilla bucket has no default air-use action; this custom action
            // intentionally handles it as drinking.
            // Region protection is checked through a synthetic BlockPlaceEvent
            // in pour(); do not let the vanilla bucket's empty-hand cancellation
            // suppress the custom placement.
            event.setCancelled(true);
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                drink(player);
            } else {
                pour(player, event);
            }
            return;
        }

        if (held.getType() == Material.BUCKET
                && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null) {
            FluidKey clicked = key(event.getClickedBlock());
            FluidCell cell = cells.get(clicked);
            if (cell != null) {
                event.setCancelled(true);
                if (!cell.source) {
                    player.sendActionBar("Only a Liquid Gold source can be collected.");
                    return;
                }
                scoop(player, clicked);
            }
        }
    }

    private void pour(Player player, PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            drink(player);
            return;
        }

        Block target = clicked.getRelative(event.getBlockFace());
        FluidKey key = key(target);
        if (cells.containsKey(key) || !canReplaceForFluid(target)) {
            player.sendActionBar("There is no space to pour Liquid Gold here.");
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                target, target.getState(), clicked, held.clone(), player, true, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled() || !placeEvent.canBuild()) {
            player.sendActionBar("You cannot place Liquid Gold at this location.");
            return;
        }

        placeWater(target, 0);
        cells.put(key, new FluidCell(0, true, null));
        ensureDisplay(key, 0, true);
        consumeFilledBucket(player);
        player.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.75F, 1.55F);
        player.spawnParticle(Particle.DUST, target.getLocation().add(0.5, 0.72, 0.5),
                8, 0.24, 0.08, 0.24, new Particle.DustOptions(Color.fromRGB(255, 211, 66), 1.15F));
    }

    private void scoop(Player player, FluidKey key) {
        FluidCell cell = cells.get(key);
        if (cell == null || !cell.source) return;

        Block block = getBlock(key);
        if (block == null || block.getType() != Material.WATER) {
            player.sendActionBar("This Liquid Gold source is not available.");
            return;
        }

        removeCell(key, true);
        consumeEmptyBucket(player);
        giveItem(player, createBucket(plugin));
        player.playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8F, 1.35F);
    }

    private void drink(Player player) {
        UUID playerId = player.getUniqueId();
        if (drinkTasks.containsKey(playerId)) return;

        final int drinkTicks = 32;
        final int[] elapsed = {0};
        player.setCooldown(Material.BUCKET, drinkTicks);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.85F, 0.85F);
        player.sendActionBar("§6Drinking Liquid Gold...");
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !isLiquidGoldBucket(
                    player.getInventory().getItemInMainHand(), plugin)) {
                cancelDrink(playerId);
                if (player.isOnline()) player.sendActionBar("§7Drinking cancelled.");
                return;
            }

            elapsed[0] += 4;
            if (elapsed[0] < drinkTicks) {
                if (elapsed[0] % 8 == 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.55F, 0.95F);
                }
                player.sendActionBar("§6Drinking Liquid Gold...");
                return;
            }

            cancelDrink(playerId);
            consumeFilledBucket(player);
            applyGoldenSurge(player);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8F, 1.35F);
            player.sendActionBar("§6Liquid Gold consumed — golden powers for 10 seconds!");
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1.0, 0),
                    18, 0.35, 0.45, 0.35, new Particle.DustOptions(Color.fromRGB(255, 214, 64), 1.35F));
        }, 1L, 4L);
        drinkTasks.put(playerId, task);
    }

    private void cancelDrink(UUID playerId) {
        BukkitTask task = drinkTasks.remove(playerId);
        if (task != null) task.cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHeldSlotChange(PlayerItemHeldEvent event) {
        cancelDrink(event.getPlayer().getUniqueId());
    }

    private void consumeFilledBucket(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
            giveItem(player, new ItemStack(Material.BUCKET));
        }
    }

    private void consumeEmptyBucket(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
        }
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        leftovers.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }

    private void applyGoldenSurge(Player player) {
        int ticks = Math.max(20, plugin.getConfig().getInt("liquid-gold.effects-duration-ticks", 200));
        addEffect(player, PotionEffectType.REGENERATION, 1, ticks);
        addEffect(player, PotionEffectType.SPEED, 1, ticks);
        addEffect(player, PotionEffectType.STRENGTH, 1, ticks);
        addEffect(player, PotionEffectType.RESISTANCE, 0, ticks);
        addEffect(player, PotionEffectType.FIRE_RESISTANCE, 0, ticks);
        addEffect(player, PotionEffectType.WATER_BREATHING, 0, ticks);
        addEffect(player, PotionEffectType.HASTE, 1, ticks);
        addEffect(player, PotionEffectType.JUMP_BOOST, 1, ticks);
        addEffect(player, PotionEffectType.ABSORPTION, 1, ticks);
        addEffect(player, PotionEffectType.NIGHT_VISION, 0, ticks);
        addEffect(player, PotionEffectType.SLOW_FALLING, 0, ticks);
        addEffect(player, PotionEffectType.DOLPHINS_GRACE, 1, ticks);
        addEffect(player, PotionEffectType.CONDUIT_POWER, 0, ticks);
    }

    private void addEffect(Player player, PotionEffectType type, int amplifier, int duration) {
        player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false, true), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition() || event.getTo() == null) return;
        updateImmersion(event.getPlayer());
    }

    /**
     * Refresh powers on a scheduled tick as well as on movement. A player who
     * stays still underwater therefore keeps the ten-second effects, while
     * leaving the Liquid Gold volume stops refresh immediately.
     */
    private void refreshImmersionEffects() {
        if (!plugin.getConfig().getBoolean("liquid-gold.enabled", true)) return;
        for (Player player : Bukkit.getOnlinePlayers()) updateImmersion(player);
    }

    private void updateImmersion(Player player) {
        UUID id = player.getUniqueId();
        boolean touching = intersectsFluid(player.getBoundingBox(), player.getWorld().getUID());
        if (!touching) {
            insidePlayers.remove(id);
            lastPowerAt.remove(id);
            return;
        }

        long now = System.currentTimeMillis();
        long last = lastPowerAt.getOrDefault(id, 0L);
        boolean firstEntry = insidePlayers.add(id);
        if (firstEntry || now - last >= 4_000L) {
            lastPowerAt.put(id, now);
            applyGoldenSurge(player);
            if (firstEntry) player.sendActionBar("§6Liquid Gold — golden powers active!");
        }
    }

    private boolean intersectsFluid(org.bukkit.util.BoundingBox box, UUID worldId) {
        int minX = floor(box.getMinX());
        int maxX = floor(box.getMaxX());
        int minY = floor(box.getMinY());
        int maxY = floor(box.getMaxY());
        int minZ = floor(box.getMinZ());
        int maxZ = floor(box.getMaxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (cells.containsKey(new FluidKey(worldId, x, y, z))) return true;
                }
            }
        }
        return false;
    }

    private int floor(double value) {
        return (int) Math.floor(value);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        insidePlayers.remove(id);
        lastPowerAt.remove(id);
        cancelDrink(id);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaFluidFlow(BlockFromToEvent event) {
        FluidKey fromKey = key(event.getBlock());
        FluidKey toKey = key(event.getToBlock());
        FluidCell fromCell = cells.get(fromKey);
        FluidCell toCell = cells.get(toKey);

        // Do not merge ordinary water into Liquid Gold or Liquid Gold into an
        // unrelated water pool. For air/replaceable blocks, leave the event
        // untouched so vanilla WATER performs its own 5-tick flow and levels.
        if (fromCell == null) {
            if (toCell != null) event.setCancelled(true);
            return;
        }
        if (event.getToBlock().getType() == Material.WATER && toCell == null) {
            event.setCancelled(true);
            return;
        }

        int maxCells = Math.max(128, plugin.getConfig().getInt("liquid-gold.max-active-cells", 4096));
        if (toCell == null && cells.size() >= maxCells) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            refreshGoldCell(fromKey);
            refreshGoldCell(toKey);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGoldSourceForm(BlockFormEvent event) {
        if (event.getNewState().getType() != Material.WATER) return;
        Block formed = event.getBlock();
        int goldSourceNeighbours = 0;
        for (BlockFace face : HORIZONTAL) {
            FluidCell neighbour = cells.get(key(formed.getRelative(face)));
            if (neighbour != null && neighbour.source) goldSourceNeighbours++;
        }
        if (goldSourceNeighbours < 2) return;

        FluidKey formedKey = key(formed);
        Bukkit.getScheduler().runTask(plugin, () -> refreshGoldCell(formedKey));
    }

    private void refreshGoldCell(FluidKey key) {
        Block block = getBlock(key);
        if (block == null || block.getType() != Material.WATER) return;
        BlockData data = block.getBlockData();
        int level = data instanceof Levelled levelled ? levelled.getLevel() : 0;
        boolean source = level == 0;
        ensureDisplay(key, level, source);
    }

    private void refreshTrackedCells() {
        for (Map.Entry<FluidKey, FluidCell> entry : new ArrayList<>(cells.entrySet())) {
            FluidKey key = entry.getKey();
            Block block = getBlock(key);
            if (block == null) continue;
            if (block.getType() != Material.WATER) {
                FluidCell stale = cells.remove(key);
                if (stale != null && stale.displayId != null) {
                    org.bukkit.entity.Entity display = Bukkit.getEntity(stale.displayId);
                    if (display != null) display.remove();
                }
                continue;
            }
            BlockData data = block.getBlockData();
            int level = data instanceof Levelled levelled ? levelled.getLevel() : 0;
            boolean source = level == 0;
            if (entry.getValue().level != level || entry.getValue().source != source) {
                ensureDisplay(key, level, source);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFluidFreeze(BlockFadeEvent event) {
        if (cells.containsKey(key(event.getBlock()))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFluidBreak(BlockBreakEvent event) {
        if (!cells.containsKey(key(event.getBlock()))) return;
        event.setCancelled(true);
        event.getPlayer().sendActionBar("Collect Liquid Gold with an empty bucket from a source block.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlaceIntoFluid(BlockPlaceEvent event) {
        if (cells.containsKey(key(event.getBlockPlaced()))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> cells.containsKey(key(block)))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> cells.containsKey(key(block)))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            scanChunk(event.getChunk());
        });
    }

    private void scanLoadedChunks() {
        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) scanChunk(chunk);
        }
    }

    private void scanChunk(org.bukkit.Chunk chunk) {
        for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
            if (!(entity instanceof ItemDisplay display)) continue;
            PersistentDataContainer data = display.getPersistentDataContainer();
            Byte marked = data.get(displayMarkerKey, PersistentDataType.BYTE);
            if (marked == null || marked == 0) continue;

            Integer x = data.get(displayXKey, PersistentDataType.INTEGER);
            Integer y = data.get(displayYKey, PersistentDataType.INTEGER);
            Integer z = data.get(displayZKey, PersistentDataType.INTEGER);
            Integer level = data.get(displayLevelKey, PersistentDataType.INTEGER);
            Byte isSource = data.get(displaySourceKey, PersistentDataType.BYTE);
            if (x == null || y == null || z == null || level == null || isSource == null) {
                display.remove();
                continue;
            }

            FluidKey key = new FluidKey(chunk.getWorld().getUID(), x, y, z);
            Block block = chunk.getWorld().getBlockAt(x, y, z);
            if (block.getType() != Material.WATER && (block.isEmpty() || block.isPassable())) {
                placeWater(block, Math.max(0, Math.min(8, level)));
            }
            if (block.getType() != Material.WATER) {
                display.remove();
                cells.remove(key);
                continue;
            }

            FluidCell previous = cells.put(key,
                    new FluidCell(level, isSource != 0, display.getUniqueId()));

            // Remove stale duplicate visual cells at the same block position.
            if (previous != null && previous.displayId != null
                    && !previous.displayId.equals(display.getUniqueId())) {
                org.bukkit.entity.Entity old = Bukkit.getEntity(previous.displayId);
                if (old != null) old.remove();
            }
            display.setItemStack(fluidDisplayItem(level));
        }
    }

    private void ensureDisplay(FluidKey key, int level, boolean source) {
        FluidCell cell = cells.get(key);
        ItemDisplay display = null;
        if (cell != null && cell.displayId != null) {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(cell.displayId);
            if (entity instanceof ItemDisplay itemDisplay && entity.isValid()) display = itemDisplay;
        }

        World world = Bukkit.getWorld(key.worldId);
        if (world == null) return;
        Block block = world.getBlockAt(key.x, key.y, key.z);

        if (display == null) {
            display = world.spawn(block.getLocation().add(0.5, 0.5, 0.5),
                    ItemDisplay.class, spawned -> {
                        spawned.setPersistent(true);
                        spawned.setInvulnerable(true);
                        spawned.setSilent(true);
                        spawned.setGravity(false);
                        spawned.setBillboard(Display.Billboard.FIXED);
                        spawned.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                        spawned.setViewRange(1.0F);
                        spawned.setShadowRadius(0.0F);
                        spawned.setShadowStrength(0.0F);
                        spawned.setDisplayWidth(1.0F);
                        spawned.setDisplayHeight(1.0F);
                    });
        }

        display.setItemStack(fluidDisplayItem(level));
        PersistentDataContainer data = display.getPersistentDataContainer();
        data.set(displayMarkerKey, PersistentDataType.BYTE, (byte) 1);
        data.set(displaySourceKey, PersistentDataType.BYTE, (byte) (source ? 1 : 0));
        data.set(displayLevelKey, PersistentDataType.INTEGER, level);
        data.set(displayXKey, PersistentDataType.INTEGER, key.x);
        data.set(displayYKey, PersistentDataType.INTEGER, key.y);
        data.set(displayZKey, PersistentDataType.INTEGER, key.z);
        cells.put(key, new FluidCell(level, source, display.getUniqueId()));
    }

    private ItemStack fluidDisplayItem(int level) {
        int modelLevel = Math.max(0, Math.min(8, level));
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(new NamespacedKey(plugin, "liquid_gold_flow_" + modelLevel));
        item.setItemMeta(meta);
        return item;
    }

    private void placeWater(Block block, int level) {
        BlockData data = Material.WATER.createBlockData();
        if (data instanceof Levelled levelled) {
            levelled.setLevel(Math.max(0, Math.min(8, level)));
            block.setBlockData(levelled, true);
        } else {
            block.setType(Material.WATER, true);
        }
    }

    private void clearCell(FluidKey key, FluidCell cell) {
        Block block = getBlock(key);
        if (block != null && block.getType() == Material.WATER) {
            block.setType(Material.AIR, true);
        }
        if (cell.displayId != null) {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(cell.displayId);
            if (entity != null) entity.remove();
        }
    }

    private void removeCell(FluidKey key, boolean clearBlock) {
        FluidCell cell = cells.remove(key);
        if (cell == null) return;
        if (clearBlock) clearCell(key, cell);
    }

    private boolean canReplaceForFluid(Block block) {
        if (block.getType() == Material.WATER || block.isLiquid()) return false;
        return block.isEmpty() || block.isPassable();
    }

    private Block getBlock(FluidKey key) {
        World world = Bukkit.getWorld(key.worldId);
        if (world == null || !world.isChunkLoaded(key.x >> 4, key.z >> 4)) return null;
        return world.getBlockAt(key.x, key.y, key.z);
    }

    private FluidKey key(Block block) {
        return new FluidKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    private record FluidKey(UUID worldId, int x, int y, int z) { }

    private static final class FluidCell {
        private final int level;
        private final boolean source;
        private final UUID displayId;

        private FluidCell(int level, boolean source, UUID displayId) {
            this.level = level;
            this.source = source;
            this.displayId = displayId;
        }
    }
}
