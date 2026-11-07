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

public record ShareResponsePayload(String fileName, UUID originalSender, UUID responder, boolean accepted) implements CustomPayload
{
    public static final CustomPayload.Id<ShareResponsePayload> PACKET_ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"share_response"));
    public static final PacketCodec<RegistryByteBuf, ShareResponsePayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, ShareResponsePayload::fileName, (PacketCodec)Uuids.PACKET_CODEC, ShareResponsePayload::originalSender, (PacketCodec)Uuids.PACKET_CODEC, ShareResponsePayload::responder, (PacketCodec)PacketCodecs.BOOLEAN, ShareResponsePayload::accepted, ShareResponsePayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

