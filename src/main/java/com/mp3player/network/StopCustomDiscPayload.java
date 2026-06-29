package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record StopCustomDiscPayload(BlockPos pos) implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "stop_custom_disc");
    public static final CustomPayload.Id<StopCustomDiscPayload> PACKET_ID = new CustomPayload.Id<>(ID);

    public static final PacketCodec<RegistryByteBuf, StopCustomDiscPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, StopCustomDiscPayload::pos,
            StopCustomDiscPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
