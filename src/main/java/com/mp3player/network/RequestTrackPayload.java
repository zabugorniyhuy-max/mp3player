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

public record RequestTrackPayload(String trackName, UUID owner) implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"request_track_c2s");
    public static final CustomPacketPayload.Type<RequestTrackPayload> PACKET_ID = new CustomPacketPayload.Type(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestTrackPayload> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, RequestTrackPayload::trackName, (StreamCodec)UUIDUtil.STREAM_CODEC, RequestTrackPayload::owner, RequestTrackPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

