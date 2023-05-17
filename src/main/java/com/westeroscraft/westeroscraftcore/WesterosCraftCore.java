package com.westeroscraft.westeroscraftcore;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WesterosCraftCore.MOD_ID)
public class WesterosCraftCore {
	public static final String MOD_ID = "westeroscraftcore";

	// Directly reference a log4j logger.
	public static final Logger log = LogManager.getLogger();

	// Says where the client and server 'proxy' code is loaded.
	public static Proxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> Proxy::new);

	public static Path modConfigPath;

	public static Block[] autoRestoreDoors = new Block[0];
	private static boolean ticking = false;
	private static int ticks = 0;
	private static long secCount = 0;
	
	private static class PendingDoorRestore {
		BlockPos pos;
		Level world;
		PendingDoorRestore(Level lvl, BlockPos p) {
			this.world = lvl;
			this.pos = p;
		}
		@Override
		public int hashCode() {
			return pos.hashCode() ^ world.hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o instanceof PendingDoorRestore) {
				PendingDoorRestore pdo = (PendingDoorRestore) o;
				return (pdo.world == this.world) && (pdo.pos.asLong() == this.pos.asLong()); 
			}
			return false;
		}
	};
	private static class RestoreInfo {
		long secCount;
		Boolean open;
	};
	private static Map<PendingDoorRestore, RestoreInfo> pendingDoorRestore = new HashMap<PendingDoorRestore, RestoreInfo>();
	
	public WesterosCraftCore() {
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		// Register the setup method for load complete
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetupEvent);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		Path configPath = FMLPaths.CONFIGDIR.get();

		modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), MOD_ID);

		// Create the config folder
		try {
			Files.createDirectory(modConfigPath);
		} catch (FileAlreadyExistsException e) {
			// Do nothing
		} catch (IOException e) {
			log.error("Failed to create westeroscraftcore config directory", e);
		}

		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC,
				MOD_ID + "/" + MOD_ID + ".toml");
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		// do something that can only be done on the client
		log.info("Got game settings {}", event.description());
	}

	@SubscribeEvent
	public void onRegisterCommandEvent(RegisterCommandsEvent event) {
	    //CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
		//PTimeCommand.register(commandDispatcher);
		//PWeatherCommand.register(commandDispatcher);
	}

	@SubscribeEvent
	public void serverStopping(ServerStoppingEvent event) {
    	// Handle any pending door closes (force immediate)
    	handlePendingDoorRestores(true);
		
	}
	
	@SubscribeEvent
    public void countTicks(ServerTickEvent event){
        if ((!ticking) || (event.phase != TickEvent.Phase.END)) return;
        ticks++;
        if (ticks >= 20) {
        	secCount++;
        	// Handle any pending door closes
        	handlePendingDoorRestores(false);
        	
        	ticks = 0;
        }
    }
	private void loadComplete(final FMLLoadCompleteEvent event) // PostRegistrationEven
	{
		List<Block> dlist = new ArrayList<Block>();
		for (String bn : Config.autoRestoreDoors.get()) {
			ResourceLocation br = new ResourceLocation(bn);
			Block blk = ForgeRegistries.BLOCKS.getValue(br);
			if ((blk != null) && (blk instanceof DoorBlock)) {
				dlist.add(blk);
			}
			else {
				log.warn("Invalid door block name: " + bn);
			}
		}
		autoRestoreDoors = dlist.toArray(new Block[0]);
		// We're ready to handle delayed actions
		ticking = true;
	}

	public static void crash(Exception x, String msg) {
		throw new ReportedException(new CrashReport(msg, x));
	}

	public static void crash(String msg) {
		crash(new Exception(), msg);
	}

	public static class Config {
		public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
		public static final ForgeConfigSpec SPEC;
		public static final ForgeConfigSpec.BooleanValue debugLog;
		public static final ForgeConfigSpec.BooleanValue debugRestore;
		public static final ForgeConfigSpec.BooleanValue disableIceMelt;
		public static final ForgeConfigSpec.BooleanValue disableSnowMelt;
		public static final ForgeConfigSpec.BooleanValue disableLeafFade;
		public static final ForgeConfigSpec.BooleanValue disableGrassFadeSpread;
		public static final ForgeConfigSpec.BooleanValue disableBambooFadeSpread;
		public static final ForgeConfigSpec.BooleanValue bambooSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disableCropGrowFade;
		public static final ForgeConfigSpec.BooleanValue cropSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disableCactusGrowFade;
		public static final ForgeConfigSpec.BooleanValue cactusSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disablePlantGrowFade;
		public static final ForgeConfigSpec.BooleanValue plantSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disableMushroomGrowFade;
		public static final ForgeConfigSpec.BooleanValue mushroomSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disableNetherWartGrowFade;
		public static final ForgeConfigSpec.BooleanValue disableStemGrowFade;
		public static final ForgeConfigSpec.BooleanValue disableSugarCaneGrowFade;
		public static final ForgeConfigSpec.BooleanValue sugarCaneSurviveAny;
		public static final ForgeConfigSpec.BooleanValue disableTNTExplode;
		public static final ForgeConfigSpec.BooleanValue disableVineGrowFade;
		public static final ForgeConfigSpec.BooleanValue vineSurviveAny;
		public static final ForgeConfigSpec.ConfigValue<List<? extends String>> autoRestoreDoors;
		public static final ForgeConfigSpec.IntValue autoRestoreTime;
		public static final ForgeConfigSpec.BooleanValue autoRestoreAllDoors;

		static {
			BUILDER.comment("Module options");
			BUILDER.push("debug");
			debugLog = BUILDER.comment("Debug logging").define("debugLog", false);
			debugRestore= BUILDER.comment("Debug block restore logging").define("debugRestore", false);
			BUILDER.pop();
			BUILDER.push("blockBehaviors");
			disableIceMelt = BUILDER.comment("Disable ice melting").define("disableIceMelt", true);
			disableSnowMelt = BUILDER.comment("Disable snow melting").define("disableSnowMelt", true);
			disableLeafFade = BUILDER.comment("Disable leaf fading").define("disableLeafFade", true);
			disableGrassFadeSpread = BUILDER.comment("Disable grass fade/spread").define("disableGrassFadeSpread", true);
			disableBambooFadeSpread = BUILDER.comment("Disable bamboo fade/spread").define("disableBambooFadeSpread", true);
			bambooSurviveAny = BUILDER.comment("Allow bamboo survive on any surface").define("bambooSurviveAny", true);
			disableCropGrowFade = BUILDER.comment("Disable crop grow/fade").define("disableCropGrowFade", true);
			cropSurviveAny = BUILDER.comment("Allow crop survive on any surface").define("cropSurviveAny", true);
			disableCactusGrowFade = BUILDER.comment("Disable cactus grow/fade").define("disableCactusGrowFade", true);
			cactusSurviveAny = BUILDER.comment("Allow cactus survive on any surface").define("cactusSurviveAny", true);
			disablePlantGrowFade = BUILDER.comment("Disable plant grow/fade").define("disablePlantGrowFade", true);
			plantSurviveAny = BUILDER.comment("Allow plant survive on any surface").define("plantSurviveAny", true);
			disableMushroomGrowFade = BUILDER.comment("Disable mushroom grow/fade").define("disableMushroomGrowFade", true);
			mushroomSurviveAny = BUILDER.comment("Allow mushroom survive on any surface").define("mushroomSurviveAny", true);
			disableNetherWartGrowFade = BUILDER.comment("Disable netherwart grow/fade").define("disableBetherWartGrowFade", true);
			disableStemGrowFade = BUILDER.comment("Disable stem grow/fade").define("disableStemGrowFade", true);
			disableSugarCaneGrowFade = BUILDER.comment("Disable sugar cane grow/fade").define("disableSugarCaneGrowFade", true);
			sugarCaneSurviveAny = BUILDER.comment("Allow sugar cane survive on any surface").define("sugarCaneSurviveAny", true);
			disableTNTExplode = BUILDER.comment("Disable TNT explode").define("disableTNTExplode", true);
			disableVineGrowFade = BUILDER.comment("Disable vine grow/fade").define("disablevineGrowFade", true);
			vineSurviveAny = BUILDER.comment("Allow vine survive on any surface").define("vineSurviveAny", true);
			BUILDER.pop();
			BUILDER.push("autoRestore");
            autoRestoreDoors = BUILDER.comment("Which door blocks to auto-restore open state (when changed by non-creative mode players)").defineList("autoRestoreDoors", 
            		Arrays.asList(), entry -> true);
            autoRestoreTime = BUILDER.comment("Number of seconds before auto-restore").defineInRange("autoRestoreTime", 30, 5, 300);
            autoRestoreAllDoors = BUILDER.comment("Auto restore all door blocks").define("autoRestoreAllDoors", false);
            BUILDER.pop();
			SPEC = BUILDER.build();
		}
	}

    @SubscribeEvent
    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
    }
    
    public static void debugLog(String msg) {
    	if (Config.debugLog.get()) { log.info(msg); }
    }
    public static void debugRestoreLog(String msg) {
    	if (Config.debugRestore.get()) { log.info(msg); }
    }
    
    public static boolean isAutoCloseDoor(Block blk) {
    	if (WesterosCraftCore.Config.autoRestoreAllDoors.get()) return true;
    	for (int i = 0; i < autoRestoreDoors.length; i++) {
    		if (autoRestoreDoors[i] == blk) return true;
    	}
    	return false;
    }
    public static void setPendingDoorRestore(Level world, BlockPos pos, boolean isOpen, boolean isCreative) {
    	PendingDoorRestore pdc = new PendingDoorRestore(world, pos);
    	RestoreInfo ri = pendingDoorRestore.get(pdc);
    	if ((ri == null) && (!isCreative)) {	// New one, and not creative mode, add record
    		ri = new RestoreInfo();
    		ri.open = isOpen; ri.secCount = secCount + WesterosCraftCore.Config.autoRestoreTime.get();
    		pendingDoorRestore.put(pdc, ri);
    		debugRestoreLog("Set door restore for " + pos + " = " + isOpen);
    	}
    	// Else, if restore record pending, but creative change, drop it
    	else if (ri != null) {
    		if (isCreative) {
    			pendingDoorRestore.remove(pdc);
    			debugRestoreLog("Drop door restore for " + pos);
    		}
    		else {	// Else, reset restore time
    			ri.secCount = secCount + WesterosCraftCore.Config.autoRestoreTime.get();
        		debugRestoreLog("Update door restore for " + pos + " = " + ri.open);
    		}
    	}
    }
    public static void handlePendingDoorRestores(boolean now) {
    	// Handle pending door close checks
    	Set<Entry<PendingDoorRestore, RestoreInfo>> kvset = pendingDoorRestore.entrySet();
    	Iterator<Entry<PendingDoorRestore, RestoreInfo>> iter = kvset.iterator();	// So that we can remove during iteration
    	while (iter.hasNext()) {
    		Entry<PendingDoorRestore, RestoreInfo> kv = iter.next();
			PendingDoorRestore pdc = kv.getKey();
    		RestoreInfo ri = kv.getValue();
    		if (now || (ri.secCount <= secCount)) {
    			BlockState bs = pdc.world.getBlockState(pdc.pos);	// Get the block state
    			if (bs != null) {
    				Block blk = bs.getBlock();
    				if ((blk instanceof DoorBlock) && isAutoCloseDoor(blk)) {	// Still right type of door
    					if (bs.getValue(DoorBlock.OPEN) != ri.open) {	// And still wrong state?
    		        		debugRestoreLog("setting " + kv.getKey().pos + " to " + ri.open);
    						DoorBlock dblk = (DoorBlock)blk;
    						dblk.setOpen(null, pdc.world, bs, pdc.pos, ri.open);
    					}
    				}
    			}
    			iter.remove();	// And remove it from the set
    		}	
    	}
    }
}
