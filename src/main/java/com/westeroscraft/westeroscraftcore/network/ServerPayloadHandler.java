package com.westeroscraft.westeroscraftcore.network;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import com.westeroscraft.westeroscraftcore.network.message.PTimeMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

//    public static void handleDataOnNetwork(final PWeatherMessage data, final IPayloadContext context) {
//    }

    /**
     * Called when a message is received of the appropriate type. CALLED BY THE
     * NETWORK THREAD, NOT THE SERVER THREAD
     */
    public static void onMessageReceived(final PTimeMessage message, IPayloadContext context) {
        final ServerPlayer sendingPlayer = (ServerPlayer) context.player();
        // Enqueue processing to happen on server thread next tick
        context.enqueueWork(() -> processMessage(sendingPlayer, message));
    }

    // This message is called from the Client thread.
    // It spawns a number of Particle particles at the target location within a
    // short range around the target location
    private static void processMessage(ServerPlayer sendingPlayer, PTimeMessage message) {
        WesterosCraftCore.log.info("Got PTimeMessage");
        return;
    }
}
