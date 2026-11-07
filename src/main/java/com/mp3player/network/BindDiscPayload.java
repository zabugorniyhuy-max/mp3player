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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BindDiscPayload(String trackName, String discId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<BindDiscPayload> ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"mp3player", (String)"bind_disc"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BindDiscPayload> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, BindDiscPayload::trackName, (StreamCodec)ByteBufCodecs.STRING_UTF8, BindDiscPayload::discId, BindDiscPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

