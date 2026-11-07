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
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShareResponsePayload(String fileName, UUID originalSender, UUID responder, boolean accepted) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ShareResponsePayload> PACKET_ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"mp3player", (String)"share_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShareResponsePayload> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, ShareResponsePayload::fileName, (StreamCodec)UUIDUtil.STREAM_CODEC, ShareResponsePayload::originalSender, (StreamCodec)UUIDUtil.STREAM_CODEC, ShareResponsePayload::responder, (StreamCodec)ByteBufCodecs.BOOL, ShareResponsePayload::accepted, ShareResponsePayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

