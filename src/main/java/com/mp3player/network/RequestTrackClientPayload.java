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

public record RequestTrackClientPayload(String trackName, UUID requester) implements CustomPayload
{
    public static final Identifier ID = Identifier.of((String)"mp3player", (String)"request_track_s2c");
    public static final CustomPayload.Id<RequestTrackClientPayload> PACKET_ID = new CustomPayload.Id(ID);
    public static final PacketCodec<RegistryByteBuf, RequestTrackClientPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, RequestTrackClientPayload::trackName, (PacketCodec)Uuids.PACKET_CODEC, RequestTrackClientPayload::requester, RequestTrackClientPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

