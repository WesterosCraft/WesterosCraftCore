package com.westeroscraft.westeroscraftcore.network;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import com.westeroscraft.westeroscraftcore.network.message.PWeatherMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public class ClientPayloadHandler {

    public static void handleDataOnNetwork(final PWeatherMessage data, final IPayloadContext context) {
        // Enqueue processing to happen on client thread next tick
        context.enqueueWork(() -> {
                    processPWeatherMessage(context.player().getCommandSenderWorld(), data);
                })
                .exceptionally(e -> {

                    context.disconnect(Component.literal("Networking failed"));
                    return null;
                });
    }


    public static PWeatherMessage.WeatherCond weatherCond = PWeatherMessage.WeatherCond.RESET;
    public static boolean savedRain = false;
    public static float savedRainLevel = 0.0F;
    public static float savedThunderLevel = 0.0F;
    // This message is called from the Client thread.
    // It spawns a number of Particle particles at the target location within a
    // short range around the target location
    private static void processPWeatherMessage(Level worldClient, PWeatherMessage message) {
        WesterosCraftCore.log.info("Got PWeatherMessage: " + message.weather);
        weatherCond = message.weather;
        switch (weatherCond) {
            case RESET:
                worldClient.getLevelData().setRaining(savedRain);
                worldClient.setRainLevel(savedRainLevel);
                worldClient.setThunderLevel(savedThunderLevel);
                break;
            case CLEAR:
                worldClient.getLevelData().setRaining(false);
                worldClient.setRainLevel(0.0F);
                worldClient.setThunderLevel(0.0F);
                break;
            case RAIN:
                worldClient.getLevelData().setRaining(true);
                worldClient.setRainLevel(1.0F);
                worldClient.setThunderLevel(0.0F);
                break;
            case THUNDER:
                worldClient.getLevelData().setRaining(true);
                worldClient.setRainLevel(1.0F);
                worldClient.setThunderLevel(1.0F);
                break;
        }
    }
}
