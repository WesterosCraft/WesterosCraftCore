package com.westeroscraft.westeroscraftcore.network.message;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PWeatherMessage(String weather) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PWeatherMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(WesterosCraftCore.MOD_ID,
			"pweather"));
	public static final StreamCodec<ByteBuf, PWeatherMessage> SIMPLE_STREAM_CODEC =
			StreamCodec.composite(
					// Stream codec and getter pair
					ByteBufCodecs.STRING_UTF8, PWeatherMessage::weather,
//					ByteBufCodecs.VAR_INT, SimpleExample::arg2,
//					ByteBufCodecs.BOOL, SimpleExample::arg3,
					PWeatherMessage::new
			);


	public static int PWEATHER_MSGID = 0x02;
	public enum WeatherCond {
		RESET, CLEAR, RAIN, THUNDER
	};
	public static final WeatherCond[] weathercondlist = WeatherCond.values();
	public WeatherCond weather;

	public PWeatherMessage(WeatherCond wthr) {
		this.weather = wthr;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.weather.ordinal());
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

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}