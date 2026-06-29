package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record FileTransferEndPayload(String fileName, UUID sender, UUID target) implements CustomPayload {
    public static final CustomPayload.Id<FileTransferEndPayload> PACKET_ID = new CustomPayload.Id<>(Identifier.of("mp3player", "file_transfer_end"));

    public static final PacketCodec<RegistryByteBuf, FileTransferEndPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FileTransferEndPayload::fileName,
            Uuids.PACKET_CODEC, FileTransferEndPayload::sender,
            Uuids.PACKET_CODEC, FileTransferEndPayload::target,
            FileTransferEndPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
