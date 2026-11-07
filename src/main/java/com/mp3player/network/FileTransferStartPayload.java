/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryByteBuf
 *  net.minecraft.network.codec.PacketCodec
 *  net.minecraft.network.codec.PacketCodecs
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.CustomPayload$Id
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.Uuids
 */
package com.mp3player.network;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record FileTransferStartPayload(String fileName, long fileSize, int totalChunks, UUID sender, UUID target) implements CustomPayload
{
    public static final CustomPayload.Id<FileTransferStartPayload> PACKET_ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"file_transfer_start"));
    public static final PacketCodec<RegistryByteBuf, FileTransferStartPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, FileTransferStartPayload::fileName, (PacketCodec)PacketCodecs.VAR_LONG, FileTransferStartPayload::fileSize, (PacketCodec)PacketCodecs.INTEGER, FileTransferStartPayload::totalChunks, (PacketCodec)Uuids.PACKET_CODEC, FileTransferStartPayload::sender, (PacketCodec)Uuids.PACKET_CODEC, FileTransferStartPayload::target, FileTransferStartPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

