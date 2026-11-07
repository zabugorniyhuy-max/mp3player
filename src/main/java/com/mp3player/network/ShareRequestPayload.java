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

public record ShareRequestPayload(String fileName, UUID sender, String senderName, UUID target) implements CustomPayload
{
    public static final CustomPayload.Id<ShareRequestPayload> PACKET_ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"share_request"));
    public static final PacketCodec<RegistryByteBuf, ShareRequestPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, ShareRequestPayload::fileName, (PacketCodec)Uuids.PACKET_CODEC, ShareRequestPayload::sender, (PacketCodec)PacketCodecs.STRING, ShareRequestPayload::senderName, (PacketCodec)Uuids.PACKET_CODEC, ShareRequestPayload::target, ShareRequestPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

