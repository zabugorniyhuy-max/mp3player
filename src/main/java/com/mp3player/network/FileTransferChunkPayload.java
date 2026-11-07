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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FileTransferChunkPayload(String fileName, int chunkIndex, byte[] data, UUID sender, UUID target) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<FileTransferChunkPayload> PACKET_ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"mp3player", (String)"file_transfer_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FileTransferChunkPayload> CODEC = StreamCodec.ofMember(FileTransferChunkPayload::write, FileTransferChunkPayload::read);

    public static void write(FileTransferChunkPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(payload.fileName());
        buf.writeInt(payload.chunkIndex());
        buf.writeByteArray(payload.data());
        buf.writeUUID(payload.sender());
        buf.writeUUID(payload.target());
    }

    public static FileTransferChunkPayload read(RegistryFriendlyByteBuf buf) {
        return new FileTransferChunkPayload(buf.readUtf(), buf.readInt(), buf.readByteArray(), buf.readUUID(), buf.readUUID());
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

