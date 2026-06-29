package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClearDiscPayload() implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "clear_disc");
    public static final CustomPayload.Id<ClearDiscPayload> PACKET_ID = new CustomPayload.Id<>(ID);

    public static final PacketCodec<RegistryByteBuf, ClearDiscPayload> CODEC = PacketCodec.unit(new ClearDiscPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
