package com.westeroscraft.westeroscraftcore.network.message;

import com.westeroscraft.westeroscraftcore.WesterosCraftCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PTimeMessage(boolean relative, int time_off) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PTimeMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(WesterosCraftCore.MOD_ID,
            "ptime"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PTimeMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PTimeMessage::relative,
            ByteBufCodecs.VAR_INT,
            PTimeMessage::time_off,
            PTimeMessage::new
    );

    public static PTimeMessage decode(FriendlyByteBuf buf) {
        try {
            return new PTimeMessage(buf.readBoolean(), buf.readInt());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            WesterosCraftCore.log.warn("Exception while reading PTimeMessage: " + e);
            return null;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(relative);
        buf.writeInt(time_off);
    }

    @Override
    public String toString() {
        return "PTimeMessage[relative=" + relative + ",time_off=" + time_off + "]";
    }
}
