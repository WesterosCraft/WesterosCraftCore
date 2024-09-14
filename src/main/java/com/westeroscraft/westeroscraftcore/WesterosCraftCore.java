package com.westeroscraft.westeroscraftcore;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.util.Tristate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WesterosCraftCore.MODID)
public class WesterosCraftCore {
    public static final String MODID = "westeroscraftcore";

    // Directly reference a log4j logger.
    public static final Logger log = LogManager.getLogger();

    // Says where the client and server 'proxy' code is loaded.
//    public static Proxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> Proxy::new);

    public static Path modConfigPath;

    public static Block[] autoRestoreDoors = new Block[0];
    public static Block[] autoRestoreGates = new Block[0];
    public static Block[] autoRestoreTrapDoors = new Block[0];
    private static boolean ticking = false;
    private static final int ticks = 0;
    private static final long secCount = 0;

    private static class PendingRestore {
        BlockPos pos;
        Level world;

        PendingRestore(Level lvl, BlockPos p) {
            this.world = lvl;
            this.pos = p;
        }

        @Override
        public int hashCode() {
            return pos.hashCode() ^ world.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PendingRestore pdo) {
                return (pdo.world == this.world) && (pdo.pos.asLong() == this.pos.asLong());
            }
            return false;
        }
    }

    private static class RestoreInfo {
        long secCount;
        Boolean open;
    }

    private static final Map<PendingRestore, RestoreInfo> pendingDoorRestore = new HashMap<PendingRestore, RestoreInfo>();
    private static final Map<PendingRestore, RestoreInfo> pendingGateRestore = new HashMap<PendingRestore, RestoreInfo>();
    private static final Map<PendingRestore, RestoreInfo> pendingTrapDoorRestore = new HashMap<PendingRestore, RestoreInfo>();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.

    public WesterosCraftCore(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(this::doClientStuff);
        // Register the setup method for load complete
//        modEventBus.addListener(this::loadComplete);
        // Register the doClientStuff method for modloading
//        modEventBus.addListener(this::onCommonSetupEvent);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        Path configPath = FMLPaths.CONFIGDIR.get();

        modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), MODID);

        // Create the config folder
        try {
            Files.createDirectory(modConfigPath);
        } catch (FileAlreadyExistsException e) {
            // Do nothing
        } catch (IOException e) {
            log.error("Failed to create westeroscraftcore config directory", e);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC,
                MODID + "/" + MODID + ".toml");
        // This will use NeoForge's ConfigurationScreen to display this mod's configs
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        log.info("Got game settings {}", event.description());
    }

//    @SubscribeEvent
//    public void onRegisterCommandEvent(RegisterCommandsEvent event) {
//        //CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
//        //PTimeCommand.register(commandDispatcher);
//        //PWeatherCommand.register(commandDispatcher);
//    }

//    @SubscribeEvent
//    public void serverStarting(ServerStartingEvent event) {
//        log.info("Register luckperms permission provider for worldedit");
//        ForgeWorldEdit.inst.setPermissionsProvider(new WorldEditPermissionProvider());
//    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        // Handle any pending door restores (force immediate)
        handlePendingDoorRestores(true);
        // Handle any pending gate restores (force immediate)
        handlePendingGateRestores(true);
        // Handle any pending trap door restores (force immediate)
        handlePendingTrapDoorRestores(true);
    }

//    @SubscribeEvent
//    public void countTicks(ServerTickEvent event){
//        if ((!ticking) || (event.phase != TickEvent.Phase.END)) return;
//        ticks++;
//        if (ticks >= 20) {
//            secCount++;
//            // Handle any pending door restores
//            handlePendingDoorRestores(false);
//            // Handle any pending gate restores
//            handlePendingGateRestores(false);
//            // Handle any pending trap door restores
//            handlePendingTrapDoorRestores(false);
//
//            ticks = 0;
//        }
//    }

//    private void loadComplete(final FMLLoadCompleteEvent event) // PostRegistrationEven
//    {
//        List<Block> dlist = new ArrayList<Block>();
//        for (String bn : Config.autoRestoreDoors) {
//            String[] split = bn.split(":");
//            ResourceLocation br = ResourceLocation.fromNamespaceAndPath(split[0], split[1]);
//            Block blk = NeoForgeRegistries.BLOCKS.getValue(br);
//            if ((blk != null) && (blk instanceof DoorBlock)) {
//                dlist.add(blk);
//            } else {
//                log.warn("Invalid door block name: " + bn);
//            }
//        }
//        autoRestoreDoors = dlist.toArray(new Block[0]);
//        List<Block> glist = new ArrayList<Block>();
//        for (String bn : Config.autoRestoreGates) {
//            String[] split = bn.split(":");
//            ResourceLocation br = ResourceLocation.fromNamespaceAndPath(split[0], split[1]);
//            Block blk = NeoForgeRegistries.BLOCKS.getValue(br);
//            if ((blk != null) && (blk instanceof FenceGateBlock)) {
//                glist.add(blk);
//            } else {
//                log.warn("Invalid fence gate block name: " + bn);
//            }
//        }
//        autoRestoreGates = glist.toArray(new Block[0]);
//        List<Block> tdlist = new ArrayList<Block>();
//        for (String bn : Config.autoRestoreTrapDoors) {
//            String[] split = bn.split(":");
//            ResourceLocation br = ResourceLocation.fromNamespaceAndPath(split[0], split[1]);
//            Block blk = NeoForgeRegistries.BLOCKS.getValue(br);
//            if ((blk != null) && (blk instanceof DoorBlock)) {
//                tdlist.add(blk);
//            } else {
//                log.warn("Invalid trap door block name: " + bn);
//            }
//        }
//        autoRestoreTrapDoors = tdlist.toArray(new Block[0]);
//
//        // We're ready to handle delayed actions
//        ticking = true;
//    }

    public static void crash(Exception x, String msg) {
        throw new ReportedException(new CrashReport(msg, x));
    }

    public static void crash(String msg) {
        crash(new Exception(), msg);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

//    @SubscribeEvent
//    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
//        checkPlayerGameMode(event.getEntity());
//    }

//    @SubscribeEvent
//    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
//        checkPlayerGameMode(event.getEntity());
//    }

//    @SubscribeEvent
//    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
//        checkPlayerGameMode(event.getEntity());
//    }
//
//    private static LuckPerms api;
//
//    public static LuckPerms getLuckPermsAPI() {
//        if (api == null) api = LuckPermsProvider.get();
//        return api;
//    }
//
//    // Check game mode of player
//    private void checkPlayerGameMode(Player player) {
//        if (player instanceof ServerPlayer sp) {
//            // If not in adventure mode, see if supposed to be forced
//            if (sp.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
//                CachedPermissionData perms = getLuckPermsAPI().getPlayerAdapter(ServerPlayer.class).getPermissionData(sp);
//                Tristate rslt = perms.checkPermission("westeroscraftcore.forceadventuremode");
//                if (rslt == Tristate.TRUE) {    // If set to true for player
//                    log.info("Player " + sp.getDisplayName().getString() + " to be forced to ADVENTURE mode");
//                    sp.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
//                }
//            }
//        }
//    }

    public static void debugLog(String msg) {
        if (Config.debugLog) {
            log.info(msg);
        }
    }

//    public static void debugRestoreLog(String msg) {
//        if (Config.debugRestore) {
//            log.info(msg);
//        }
//    }

    public static boolean isAutoRestoreDoor(Block blk) {
        if (Config.autoRestoreAllDoors) return true;
        for (int i = 0; i < autoRestoreDoors.length; i++) {
            if (autoRestoreDoors[i] == blk) return true;
        }
        return false;
    }

    public static boolean isAutoRestoreGate(Block blk) {
        if (Config.autoRestoreAllGates) return true;
        for (int i = 0; i < autoRestoreGates.length; i++) {
            if (autoRestoreGates[i] == blk) return true;
        }
        return false;
    }

    public static boolean isAutoRestoreTrapDoor(Block blk) {
        if (Config.autoRestoreAllTrapdoors) return true;
        for (int i = 0; i < autoRestoreTrapDoors.length; i++) {
            if (autoRestoreTrapDoors[i] == blk) return true;
        }
        return false;
    }

    public static void setPendingDoorRestore(Level world, BlockPos pos, boolean isOpen, boolean isCreative) {
        PendingRestore pdc = new PendingRestore(world, pos);
        RestoreInfo ri = pendingDoorRestore.get(pdc);
        if ((ri == null) && (!isCreative)) {    // New one, and not creative mode, add record
            ri = new RestoreInfo();
            ri.open = isOpen;
            ri.secCount = secCount + Config.autoRestoreTime;
            pendingDoorRestore.put(pdc, ri);
//            debugRestoreLog("Set door restore for " + pos + " = " + isOpen);
        }
        // Else, if restore record pending, but creative change, drop it
        else if (ri != null) {
            if (isCreative) {
                pendingDoorRestore.remove(pdc);
//                debugRestoreLog("Drop door restore for " + pos);
            } else {    // Else, reset restore time
                ri.secCount = secCount + Config.autoRestoreTime;
//                debugRestoreLog("Update door restore for " + pos + " = " + ri.open);
            }
        }
    }

    public static void setPendingGateRestore(Level world, BlockPos pos, boolean isOpen, boolean isCreative) {
        PendingRestore pdc = new PendingRestore(world, pos);
        RestoreInfo ri = pendingGateRestore.get(pdc);
        if ((ri == null) && (!isCreative)) {    // New one, and not creative mode, add record
            ri = new RestoreInfo();
            ri.open = isOpen;
            ri.secCount = secCount + Config.autoRestoreTime;
            pendingGateRestore.put(pdc, ri);
//            debugRestoreLog("Set gate restore for " + pos + " = " + isOpen);
        }
        // Else, if restore record pending, but creative change, drop it
        else if (ri != null) {
            if (isCreative) {
                pendingGateRestore.remove(pdc);
//                debugRestoreLog("Drop gate restore for " + pos);
            } else {    // Else, reset restore time
                ri.secCount = secCount + Config.autoRestoreTime;
//                debugRestoreLog("Update gate restore for " + pos + " = " + ri.open);
            }
        }
    }

    public static void setPendingTrapDoorRestore(Level world, BlockPos pos, boolean isOpen, boolean isCreative) {
        PendingRestore pdc = new PendingRestore(world, pos);
        RestoreInfo ri = pendingTrapDoorRestore.get(pdc);
        if ((ri == null) && (!isCreative)) {    // New one, and not creative mode, add record
            ri = new RestoreInfo();
            ri.open = isOpen;
            ri.secCount = secCount + Config.autoRestoreTime;
            pendingTrapDoorRestore.put(pdc, ri);
//            debugRestoreLog("Set trap door restore for " + pos + " = " + isOpen);
        }
        // Else, if restore record pending, but creative change, drop it
        else if (ri != null) {
            if (isCreative) {
                pendingTrapDoorRestore.remove(pdc);
//                debugRestoreLog("Drop trap door restore for " + pos);
            } else {    // Else, reset restore time
                ri.secCount = secCount + Config.autoRestoreTime;
//                debugRestoreLog("Update trap door restore for " + pos + " = " + ri.open);
            }
        }
    }

    public static void handlePendingDoorRestores(boolean now) {
        // Handle pending door close checks
        Set<Entry<PendingRestore, RestoreInfo>> kvset = pendingDoorRestore.entrySet();
        Iterator<Entry<PendingRestore, RestoreInfo>> iter = kvset.iterator();    // So that we can remove during iteration
        while (iter.hasNext()) {
            Entry<PendingRestore, RestoreInfo> kv = iter.next();
            PendingRestore pdc = kv.getKey();
            RestoreInfo ri = kv.getValue();
            if (now || (ri.secCount <= secCount)) {
                BlockState bs = pdc.world.getBlockState(pdc.pos);    // Get the block state
                if (bs != null) {
                    Block blk = bs.getBlock();
                    if ((blk instanceof DoorBlock) && isAutoRestoreDoor(blk)) {    // Still right type of door
                        if (bs.getValue(DoorBlock.OPEN) != ri.open) {    // And still wrong state?
//                            debugRestoreLog("setting " + kv.getKey().pos + " to " + ri.open);
                            DoorBlock dblk = (DoorBlock) blk;
                            dblk.setOpen(null, pdc.world, bs, pdc.pos, ri.open);
                        }
                    }
                }
                iter.remove();    // And remove it from the set
            }
        }
    }

    public static void handlePendingGateRestores(boolean now) {
        // Handle pending gate close checks
        Set<Entry<PendingRestore, RestoreInfo>> kvset = pendingGateRestore.entrySet();
        Iterator<Entry<PendingRestore, RestoreInfo>> iter = kvset.iterator();    // So that we can remove during iteration
        while (iter.hasNext()) {
            Entry<PendingRestore, RestoreInfo> kv = iter.next();
            PendingRestore pdc = kv.getKey();
            RestoreInfo ri = kv.getValue();
            if (now || (ri.secCount <= secCount)) {
                BlockState bs = pdc.world.getBlockState(pdc.pos);    // Get the block state
                if (bs != null) {
                    Block blk = bs.getBlock();
                    if ((blk instanceof FenceGateBlock) && isAutoRestoreGate(blk)) {    // Still right type of door
                        if (bs.getValue(FenceGateBlock.OPEN) != ri.open) {    // And still wrong state?
//                            debugRestoreLog("setting " + kv.getKey().pos + " to " + ri.open);
                            bs = bs.setValue(FenceGateBlock.OPEN, ri.open);
                            pdc.world.setBlock(pdc.pos, bs, 10);
                            pdc.world.levelEvent(null, ri.open ? 1008 : 1014, pdc.pos, 0);
                            pdc.world.gameEvent(null, ri.open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pdc.pos);
                        }
                    }
                }
                iter.remove();    // And remove it from the set
            }
        }
    }

    public static void handlePendingTrapDoorRestores(boolean now) {
        // Handle pending door close checks
        Set<Entry<PendingRestore, RestoreInfo>> kvset = pendingTrapDoorRestore.entrySet();
        Iterator<Entry<PendingRestore, RestoreInfo>> iter = kvset.iterator();    // So that we can remove during iteration
        while (iter.hasNext()) {
            Entry<PendingRestore, RestoreInfo> kv = iter.next();
            PendingRestore pdc = kv.getKey();
            RestoreInfo ri = kv.getValue();
            if (now || (ri.secCount <= secCount)) {
                BlockState bs = pdc.world.getBlockState(pdc.pos);    // Get the block state
                if (bs != null) {
                    Block blk = bs.getBlock();
                    if ((blk instanceof TrapDoorBlock) && isAutoRestoreTrapDoor(blk)) {    // Still right type of door
                        if (bs.getValue(TrapDoorBlock.OPEN) != ri.open) {    // And still wrong state?
//                            debugRestoreLog("setting " + kv.getKey().pos + " to " + ri.open);
                            TrapDoorBlock dblk = (TrapDoorBlock) blk;

                            bs = bs.setValue(TrapDoorBlock.OPEN, ri.open);
                            pdc.world.setBlock(pdc.pos, bs, 10);
//                            dblk.playSound((Player) null, pdc.world, pdc.pos, ri.open);
                            if (bs.getValue(TrapDoorBlock.WATERLOGGED)) {
                                pdc.world.scheduleTick(pdc.pos, Fluids.WATER, Fluids.WATER.getTickDelay(pdc.world));
                            }
                        }
                    }
                }
                iter.remove();    // And remove it from the set
            }
        }
    }

}