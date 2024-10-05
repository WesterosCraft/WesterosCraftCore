package com.westeroscraft.westeroscraftcore.network;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import com.westeroscraft.westeroscraftcore.network.message.PWeatherMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void init(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(WesterosCraftCore.MOD_ID);
        // Execute on network thread
        registrar = registrar.executesOn(HandlerThread.NETWORK);
        registrar.playToClient(
                PWeatherMessage.TYPE,
                PWeatherMessage.STREAM_CODEC,
                ClientPayloadHandler::onPWeatherMessageRecieved
        );
    }
}
