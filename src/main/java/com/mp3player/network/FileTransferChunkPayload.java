/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryByteBuf
 *  net.minecraft.network.codec.PacketCodec
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.CustomPayload$Id
 *  net.minecraft.util.Identifier
 */
package com.mp3player.network;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FileTransferChunkPayload(String fileName, int chunkIndex, byte[] data, UUID sender, UUID target) implements CustomPayload
{
    public static final CustomPayload.Id<FileTransferChunkPayload> PACKET_ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"file_transfer_chunk"));
    public static final PacketCodec<RegistryByteBuf, FileTransferChunkPayload> CODEC = PacketCodec.of(FileTransferChunkPayload::write, FileTransferChunkPayload::read);

    public static void write(FileTransferChunkPayload payload, RegistryByteBuf buf) {
        buf.writeString(payload.fileName());
        buf.writeInt(payload.chunkIndex());
        buf.writeByteArray(payload.data());
        buf.writeUuid(payload.sender());
        buf.writeUuid(payload.target());
    }

    public static FileTransferChunkPayload read(RegistryByteBuf buf) {
        return new FileTransferChunkPayload(buf.readString(), buf.readInt(), buf.readByteArray(), buf.readUuid(), buf.readUuid());
    }

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

