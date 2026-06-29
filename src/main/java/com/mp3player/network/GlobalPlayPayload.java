package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GlobalPlayPayload(String trackName, long position, int range, float volume) implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "global_play_c2s");
    public static final CustomPayload.Id<GlobalPlayPayload> PACKET_ID = new CustomPayload.Id<>(ID);
    
    public static final PacketCodec<RegistryByteBuf, GlobalPlayPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, GlobalPlayPayload::trackName,
        PacketCodecs.VAR_LONG, GlobalPlayPayload::position,
        PacketCodecs.INTEGER, GlobalPlayPayload::range,
        PacketCodecs.FLOAT, GlobalPlayPayload::volume,
        GlobalPlayPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
