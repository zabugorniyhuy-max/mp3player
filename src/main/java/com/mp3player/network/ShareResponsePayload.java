package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ShareResponsePayload(String fileName, UUID originalSender, UUID responder, boolean accepted) implements CustomPayload {
    public static final CustomPayload.Id<ShareResponsePayload> PACKET_ID = new CustomPayload.Id<>(Identifier.of("mp3player", "share_response"));

    public static final PacketCodec<RegistryByteBuf, ShareResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ShareResponsePayload::fileName,
            Uuids.PACKET_CODEC, ShareResponsePayload::originalSender,
            Uuids.PACKET_CODEC, ShareResponsePayload::responder,
            PacketCodecs.BOOL, ShareResponsePayload::accepted,
            ShareResponsePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
