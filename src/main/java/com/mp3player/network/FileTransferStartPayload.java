package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record FileTransferStartPayload(String fileName, long fileSize, int totalChunks, UUID sender, UUID target) implements CustomPayload {
    public static final CustomPayload.Id<FileTransferStartPayload> PACKET_ID = new CustomPayload.Id<>(Identifier.of("mp3player", "file_transfer_start"));

    public static final PacketCodec<RegistryByteBuf, FileTransferStartPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FileTransferStartPayload::fileName,
            PacketCodecs.VAR_LONG, FileTransferStartPayload::fileSize,
            PacketCodecs.INTEGER, FileTransferStartPayload::totalChunks,
            Uuids.PACKET_CODEC, FileTransferStartPayload::sender,
            Uuids.PACKET_CODEC, FileTransferStartPayload::target,
            FileTransferStartPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
