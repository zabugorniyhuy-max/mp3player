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

public record GlobalPlayClientPayload(UUID sender, String trackName, long position, int range, float volume) implements CustomPayload
{
    public static final Identifier ID = Identifier.of((String)"mp3player", (String)"global_play_s2c");
    public static final CustomPayload.Id<GlobalPlayClientPayload> PACKET_ID = new CustomPayload.Id(ID);
    public static final PacketCodec<RegistryByteBuf, GlobalPlayClientPayload> CODEC = PacketCodec.tuple((PacketCodec)Uuids.PACKET_CODEC, GlobalPlayClientPayload::sender, (PacketCodec)PacketCodecs.STRING, GlobalPlayClientPayload::trackName, (PacketCodec)PacketCodecs.VAR_LONG, GlobalPlayClientPayload::position, (PacketCodec)PacketCodecs.INTEGER, GlobalPlayClientPayload::range, (PacketCodec)PacketCodecs.FLOAT, GlobalPlayClientPayload::volume, GlobalPlayClientPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

