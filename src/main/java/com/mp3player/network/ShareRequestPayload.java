package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ShareRequestPayload(String fileName, UUID sender, String senderName, UUID target) implements CustomPayload {
    public static final CustomPayload.Id<ShareRequestPayload> PACKET_ID = new CustomPayload.Id<>(Identifier.of("mp3player", "share_request"));

    public static final PacketCodec<RegistryByteBuf, ShareRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ShareRequestPayload::fileName,
            Uuids.PACKET_CODEC, ShareRequestPayload::sender,
            PacketCodecs.STRING, ShareRequestPayload::senderName,
            Uuids.PACKET_CODEC, ShareRequestPayload::target,
            ShareRequestPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
