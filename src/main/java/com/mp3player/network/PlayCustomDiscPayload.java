package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PlayCustomDiscPayload(BlockPos pos, String trackName) implements CustomPayload {
    public static final CustomPayload.Id<PlayCustomDiscPayload> ID = new CustomPayload.Id<>(Identifier.of("mp3player", "play_custom_disc"));
    public static final PacketCodec<RegistryByteBuf, PlayCustomDiscPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, PlayCustomDiscPayload::pos,
        PacketCodecs.STRING, PlayCustomDiscPayload::trackName,
        PlayCustomDiscPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
