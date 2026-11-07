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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClearDiscPayload() implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"clear_disc");
    public static final CustomPacketPayload.Type<ClearDiscPayload> PACKET_ID = new CustomPacketPayload.Type(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearDiscPayload> CODEC = StreamCodec.unit(new ClearDiscPayload());

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

