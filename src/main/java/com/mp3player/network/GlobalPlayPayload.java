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
 */
package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GlobalPlayPayload(String trackName, long position, int range, float volume) implements CustomPayload
{
    public static final Identifier ID = Identifier.of((String)"mp3player", (String)"global_play_c2s");
    public static final CustomPayload.Id<GlobalPlayPayload> PACKET_ID = new CustomPayload.Id(ID);
    public static final PacketCodec<RegistryByteBuf, GlobalPlayPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, GlobalPlayPayload::trackName, (PacketCodec)PacketCodecs.VAR_LONG, GlobalPlayPayload::position, (PacketCodec)PacketCodecs.INTEGER, GlobalPlayPayload::range, (PacketCodec)PacketCodecs.FLOAT, GlobalPlayPayload::volume, GlobalPlayPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

