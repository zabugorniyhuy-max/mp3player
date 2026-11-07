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

public record FileTransferEndPayload(String fileName, UUID sender, UUID target) implements CustomPayload
{
    public static final CustomPayload.Id<FileTransferEndPayload> PACKET_ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"file_transfer_end"));
    public static final PacketCodec<RegistryByteBuf, FileTransferEndPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, FileTransferEndPayload::fileName, (PacketCodec)Uuids.PACKET_CODEC, FileTransferEndPayload::sender, (PacketCodec)Uuids.PACKET_CODEC, FileTransferEndPayload::target, FileTransferEndPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

