package com.westeroscraft.westeroscraftcore;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = WesterosCraftCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // BLOCK BEHAVIORS

    private static final ModConfigSpec.BooleanValue DEBUG_LOG = BUILDER
            .translation("westeroscraftcore.configuration.debugLog").comment("Enable debug logging")
            .define("debugLog", false);

    private static final ModConfigSpec.BooleanValue DISABLE_ICE_MELT = BUILDER
            .translation("westeroscraftcore.configuration.disableIceMelt").comment("Disable ice melting")
            .define("disableIceMelt", true);

    private static final ModConfigSpec.BooleanValue DISABLE_SNOW_MELT = BUILDER
            .translation("westeroscraftcore.configuration.disableSnowMelt").comment("Disable snow melting")
            .define("disableSnowMelt", true);

    private static final ModConfigSpec.BooleanValue DISABLE_LEAF_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableLeafFade").comment("Disable leaf fading")
            .define("disableLeafFade", true);

    private static final ModConfigSpec.BooleanValue DISABLE_GRASS_FADE_SPREAD = BUILDER
            .translation("westeroscraftcore.configuration.disableGrassFadeSpread").comment("Disable grass fade/spread")
            .define("disableGrassFadeSpread", true);

    private static final ModConfigSpec.BooleanValue DISABLE_BAMBOO_FADE_SPREAD = BUILDER
            .translation("westeroscraftcore.configuration.disableBambooFadeSpread").comment("Disable bamboo fade/spread")
            .define("disableBambooFadeSpread", true);

    private static final ModConfigSpec.BooleanValue BAMBOO_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.bambooSurviveAny").comment("Allow bamboo to survive on any surface")
            .define("bambooSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_CROP_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableCropGrowFade").comment("Disable crop grow/fade")
            .define("disableCropGrowFade", true);

    private static final ModConfigSpec.BooleanValue CROP_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.cropSurviveAny").comment("Allow crops to survive on any surface")
            .define("cropSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_CACTUS_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableCactusGrowFade").comment("Disable cactus grow/fade")
            .define("disableCactusGrowFade", true);

    private static final ModConfigSpec.BooleanValue CACTUS_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.cactusSurviveAny").comment("Allow cactus to survive on any surface")
            .define("cactusSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_PLANT_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disablePlantGrowFade").comment("Disable plant grow/fade")
            .define("disablePlantGrowFade", true);

    private static final ModConfigSpec.BooleanValue PLANT_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.plantSurviveAny").comment("Allow plants to survive on any surface")
            .define("plantSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_MUSHROOM_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableMushroomGrowFade").comment("Disable mushroom grow/fade")
            .define("disableMushroomGrowFade", true);

    private static final ModConfigSpec.BooleanValue MUSHROOM_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.mushroomSurviveAny").comment("Allow mushrooms to survive on any surface")
            .define("mushroomSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_NETHER_WART_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableBetherWartGrowFade").comment("Disable nether wart grow/fade")
            .define("disableBetherWartGrowFade", true);

    private static final ModConfigSpec.BooleanValue DISABLE_STEM_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableStemGrowFade").comment("Disable stem grow/fade")
            .define("disableStemGrowFade", true);

    private static final ModConfigSpec.BooleanValue DISABLE_SUGAR_CANE_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disableSugarCaneGrowFade").comment("Disable sugar cane grow/fade")
            .define("disableSugarCaneGrowFade", true);

    private static final ModConfigSpec.BooleanValue SUGAR_CANE_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.sugarCaneSurviveAny").comment("Allow sugar cane to survive on any surface")
            .define("sugarCaneSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_TNT_EXPLODE = BUILDER
            .translation("westeroscraftcore.configuration.disableTNTExplode").comment("Disable TNT explosions")
            .define("disableTNTExplode", true);

    private static final ModConfigSpec.BooleanValue DISABLE_VINE_GROW_FADE = BUILDER
            .translation("westeroscraftcore.configuration.disablevineGrowFade").comment("Disable vine grow/fade")
            .define("disablevineGrowFade", true);

    private static final ModConfigSpec.BooleanValue VINE_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.vineSurviveAny").comment("Allow vine to survive on any surface")
            .define("vineSurviveAny", true);

    private static final ModConfigSpec.BooleanValue SNOW_LAYER_SURVIVE_ANY = BUILDER
            .translation("westeroscraftcore.configuration.snowLayerSurviveAny").comment("Allow snow layer to survive on any surface")
            .define("snowLayerSurviveAny", true);

    private static final ModConfigSpec.BooleanValue DISABLE_FARM_STOMPING = BUILDER
            .translation("westeroscraftcore.configuration.disableFarmStomping").comment("Disable farmland stomping")
            .define("disableFarmStomping", true);

    private static final ModConfigSpec.BooleanValue BLOCK_HANGING_ITEM_CHANGES = BUILDER
            .translation("westeroscraftcore.configuration.blockHangingItemChanges").comment("Prevent item frame, picture interaction outside of creative mode")
            .define("blockHangingItemChanges", true);

    private static final ModConfigSpec.BooleanValue DISABLE_FLUID_TICKING = BUILDER
            .translation("westeroscraftcore.configuration.disableFluidTicking").comment("Disable fluid ticking")
            .define("disableFluidTicking", true);

    private static final ModConfigSpec.BooleanValue DISABLE_FALLING_BLOCKS = BUILDER
            .translation("westeroscraftcore.configuration.disableFallingBlocks").comment("Disable falling blocks")
            .define("disableFallingBlocks", true);


    // AUTO RESTORE
    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> AUTO_RESTORE_DOORS = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreDoors")
            .comment("Which door blocks to auto-restore open state (when changed by non-creative mode players).")
            .defineListAllowEmpty("autoRestoreDoors", List.of(), null, Config::validateListName);

    private static final ModConfigSpec.IntValue AUTO_RESTORE_TIME = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreTime")
            .comment("Number of seconds before auto-restore")
            .defineInRange("autoRestoreTime", 30, 5, 300);

    private static final ModConfigSpec.BooleanValue AUTO_RESTORE_ALL_DOORS = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreAllDoors")
            .comment("Auto restore all door blocks")
            .define("autoRestoreAllDoors", false);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> AUTO_RESTORE_GATES = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreGates")
            .comment("Which fence gate blocks to auto-restore open state (when changed by non-creative mode players)")
            .defineListAllowEmpty("autoRestoreGates", List.of(), Config::validateListName);

    private static final ModConfigSpec.BooleanValue AUTO_RESTORE_ALL_GATES = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreAllGates")
            .comment("Auto restore all gate blocks")
            .define("autoRestoreAllGates", false);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> AUTO_RESTORE_TRAP_DOORS = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreTrapDoors")
            .comment("Auto restore all trap door blocks")
            .defineListAllowEmpty("autoRestoreTrapDoors", List.of(), Config::validateListName);

    private static final ModConfigSpec.BooleanValue AUTO_RESTORE_ALL_TRAP_DOORS = BUILDER
            .translation("westeroscraftcore.configuration.autoRestoreAllTrapDoors")
            .comment("Auto restore all trap door blocks")
            .define("autoRestoreAllTrapDoors", false);

// PLAYER MODS

    private static final ModConfigSpec.BooleanValue DISABLE_HUNGER = BUILDER
            .translation("westeroscraftcore.configuration.disableHunger")
            .comment("Disable hunger on players")
            .define("disableHunger", true);

    private static final ModConfigSpec.BooleanValue BLOCK_WITHER_SPAWN = BUILDER
            .translation("westeroscraftcore.configuration.blockWitherSpawn")
            .comment("Block Wither from spawning globally")
            .define("blockWitherSpawn", true);

    // a list of strings that are treated as resource locations for items
//    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean debugLog;
    public static boolean disableIceMelt;
    public static boolean disableSnowMelt;
    public static boolean disableLeafFade;
    public static boolean disableGrassFadeSpread;
    public static boolean disableBambooFadeSpread;
    public static boolean bambooSurviveAny;
    public static boolean disableCropGrowFade;
    public static boolean cropSurviveAny;
    public static boolean disableCactusGrowFade;
    public static boolean cactusSurviveAny;
    public static boolean disablePlantGrowFade;
    public static boolean plantSurviveAny;
    public static boolean disableMushroomGrowFade;
    public static boolean mushroomSurviveAny;
    public static boolean disableNetherWartGrowFade;
    public static boolean disableStemGrowFade;
    public static boolean disableSugarCaneGrowFade;
    public static boolean sugarCaneSurviveAny;
    public static boolean disableTNTExplode;
    public static boolean disableVineGrowFade;
    public static boolean vineSurviveAny;
    public static boolean snowLayerSurviveAny;
    public static boolean disableFarmStomping;
    public static boolean blockHangingItemChanges;
    public static boolean disableFluidTicking;
    public static boolean disableFallingBlocks;
    public static List<String> autoRestoreDoors;
    public static int autoRestoreTime;
    public static boolean autoRestoreAllDoors;
    public static List<String> autoRestoreGates;
    public static boolean autoRestoreAllGates;
    public static List<String> autoRestoreTrapdoors;
    public static boolean autoRestoreAllTrapdoors;
    public static boolean disableHunger;
    public static boolean blockWitherSpawn;

//    public static Set<Item> items;

    private static boolean validateListName(final Object obj) {
        return obj instanceof String listName && BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(listName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        debugLog = DEBUG_LOG.get();
        disableIceMelt = DISABLE_ICE_MELT.get();
        disableSnowMelt = DISABLE_SNOW_MELT.get();
        disableLeafFade = DISABLE_LEAF_FADE.get();
        disableGrassFadeSpread = DISABLE_GRASS_FADE_SPREAD.get();
        disableBambooFadeSpread = DISABLE_BAMBOO_FADE_SPREAD.get();
        bambooSurviveAny = BAMBOO_SURVIVE_ANY.get();
        disableCropGrowFade = DISABLE_CROP_GROW_FADE.get();
        cropSurviveAny = CROP_SURVIVE_ANY.get();
        disableCactusGrowFade = DISABLE_CACTUS_GROW_FADE.get();
        cactusSurviveAny = CACTUS_SURVIVE_ANY.get();
        disablePlantGrowFade = DISABLE_PLANT_GROW_FADE.get();
        plantSurviveAny = PLANT_SURVIVE_ANY.get();
        disableMushroomGrowFade = DISABLE_MUSHROOM_GROW_FADE.get();
        mushroomSurviveAny = MUSHROOM_SURVIVE_ANY.get();
        disableNetherWartGrowFade = DISABLE_NETHER_WART_GROW_FADE.get();
        disableStemGrowFade = DISABLE_STEM_GROW_FADE.get();
        disableSugarCaneGrowFade = DISABLE_SUGAR_CANE_GROW_FADE.get();
        sugarCaneSurviveAny = SUGAR_CANE_SURVIVE_ANY.get();
        disableTNTExplode = DISABLE_TNT_EXPLODE.get();
        disableVineGrowFade = DISABLE_VINE_GROW_FADE.get();
        vineSurviveAny = VINE_SURVIVE_ANY.get();
        snowLayerSurviveAny = SNOW_LAYER_SURVIVE_ANY.get();
        disableFarmStomping = DISABLE_FARM_STOMPING.get();
        blockHangingItemChanges = BLOCK_HANGING_ITEM_CHANGES.get();
        disableFluidTicking = DISABLE_FLUID_TICKING.get();
        disableFallingBlocks = DISABLE_FALLING_BLOCKS.get();
//        autoRestoreDoors = AUTO_RESTORE_DOORS.get();
        autoRestoreTime = AUTO_RESTORE_TIME.get();
        autoRestoreAllDoors = AUTO_RESTORE_ALL_DOORS.get();
//        autoRestoreGates = AUTO_RESTORE_GATES.get();
        autoRestoreAllGates = AUTO_RESTORE_ALL_GATES.get();
//        autoRestoreTrapdoors = AUTO_RESTORE_TRAP_DOORS.get();
        autoRestoreAllTrapdoors = AUTO_RESTORE_ALL_TRAP_DOORS.get();
        disableHunger = DISABLE_HUNGER.get();
        blockWitherSpawn = BLOCK_WITHER_SPAWN.get();
    }
}
