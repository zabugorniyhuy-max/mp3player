package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BindDiscPayload(String trackName, String discId) implements CustomPayload {
    public static final CustomPayload.Id<BindDiscPayload> ID = new CustomPayload.Id<>(Identifier.of("mp3player", "bind_disc"));
    public static final PacketCodec<RegistryByteBuf, BindDiscPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, BindDiscPayload::trackName,
        PacketCodecs.STRING, BindDiscPayload::discId,
        BindDiscPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
