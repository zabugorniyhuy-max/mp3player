package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record GlobalPlayClientPayload(UUID sender, String trackName, long position, int range, float volume) implements CustomPayload {
    public static final Identifier ID = Identifier.of("mp3player", "global_play_s2c");
    public static final CustomPayload.Id<GlobalPlayClientPayload> PACKET_ID = new CustomPayload.Id<>(ID);
    
    public static final PacketCodec<RegistryByteBuf, GlobalPlayClientPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, GlobalPlayClientPayload::sender,
        PacketCodecs.STRING, GlobalPlayClientPayload::trackName,
        PacketCodecs.VAR_LONG, GlobalPlayClientPayload::position,
        PacketCodecs.INTEGER, GlobalPlayClientPayload::range,
        PacketCodecs.FLOAT, GlobalPlayClientPayload::volume,
        GlobalPlayClientPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
