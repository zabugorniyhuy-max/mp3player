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

public record BindDiscPayload(String trackName, String discId) implements CustomPayload
{
    public static final CustomPayload.Id<BindDiscPayload> ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"bind_disc"));
    public static final PacketCodec<RegistryByteBuf, BindDiscPayload> CODEC = PacketCodec.tuple((PacketCodec)PacketCodecs.STRING, BindDiscPayload::trackName, (PacketCodec)PacketCodecs.STRING, BindDiscPayload::discId, BindDiscPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

