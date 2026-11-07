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

public record GlobalPlayClientPayload(UUID sender, String trackName, long position, int range, float volume) implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"global_play_s2c");
    public static final CustomPacketPayload.Type<GlobalPlayClientPayload> PACKET_ID = new CustomPacketPayload.Type(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, GlobalPlayClientPayload> CODEC = StreamCodec.composite((StreamCodec)UUIDUtil.STREAM_CODEC, GlobalPlayClientPayload::sender, (StreamCodec)ByteBufCodecs.STRING_UTF8, GlobalPlayClientPayload::trackName, (StreamCodec)ByteBufCodecs.VAR_LONG, GlobalPlayClientPayload::position, (StreamCodec)ByteBufCodecs.INT, GlobalPlayClientPayload::range, (StreamCodec)ByteBufCodecs.FLOAT, GlobalPlayClientPayload::volume, GlobalPlayClientPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

