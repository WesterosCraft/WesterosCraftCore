package com.westeroscraft.westeroscraftcore.network.message;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record PWeatherMessage(WeatherCond weather) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PWeatherMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(WesterosCraftCore.MOD_ID,
            "pweather"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PWeatherMessage> STREAM_CODEC = StreamCodec.composite(
            WeatherCond.STREAM_CODEC,
            PWeatherMessage::weather,
            PWeatherMessage::new
    );

    public enum WeatherCond {
        RESET, CLEAR, RAIN, THUNDER;

        public static final StreamCodec<FriendlyByteBuf, WeatherCond> STREAM_CODEC = NeoForgeStreamCodecs
                .enumCodec(WeatherCond.class);
    }

    public static final WeatherCond[] weathercondlist = WeatherCond.values();

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(weather.ordinal());
    }

    public static PWeatherMessage decode(FriendlyByteBuf buf) {
        try {
            int idx = buf.readInt();
            return new PWeatherMessage(weathercondlist[idx]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            WesterosCraftCore.log.warn("Exception while reading PTimeMessage: " + e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "PWeatherMessage[" + weather + "]";
    }
}