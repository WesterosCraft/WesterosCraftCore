package com.westeroscraft.westeroscraftcore;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WesterosCraftCore.MOD_ID)
public class WesterosCraftCore {
	public static final String MOD_ID = "westeroscraftcore";

	// Directly reference a log4j logger.
	public static final Logger log = LogManager.getLogger();

	// Says where the client and server 'proxy' code is loaded.
	public static Proxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> Proxy::new);

	public static Path modConfigPath;
	
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
	    CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
		//PTimeCommand.register(commandDispatcher);
		//PWeatherCommand.register(commandDispatcher);
	}
	
	private void loadComplete(final FMLLoadCompleteEvent event) // PostRegistrationEven
	{

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

		static {
			BUILDER.comment("Module options");
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
			SPEC = BUILDER.build();
		}
	}

    @SubscribeEvent
    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
    }
}
