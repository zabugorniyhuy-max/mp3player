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

public record GlobalPlayPayload(String trackName, long position, int range, float volume) implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"global_play_c2s");
    public static final CustomPacketPayload.Type<GlobalPlayPayload> PACKET_ID = new CustomPacketPayload.Type(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, GlobalPlayPayload> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, GlobalPlayPayload::trackName, (StreamCodec)ByteBufCodecs.VAR_LONG, GlobalPlayPayload::position, (StreamCodec)ByteBufCodecs.INT, GlobalPlayPayload::range, (StreamCodec)ByteBufCodecs.FLOAT, GlobalPlayPayload::volume, GlobalPlayPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

