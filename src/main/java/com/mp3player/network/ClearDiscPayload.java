/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryByteBuf
 *  net.minecraft.network.codec.PacketCodec
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.CustomPayload$Id
 *  net.minecraft.util.Identifier
 */
package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClearDiscPayload() implements CustomPayload
{
    public static final Identifier ID = Identifier.of((String)"mp3player", (String)"clear_disc");
    public static final CustomPayload.Id<ClearDiscPayload> PACKET_ID = new CustomPayload.Id(ID);
    public static final PacketCodec<RegistryByteBuf, ClearDiscPayload> CODEC = PacketCodec.unit(new ClearDiscPayload());

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

