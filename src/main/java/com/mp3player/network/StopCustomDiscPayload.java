/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryByteBuf
 *  net.minecraft.network.codec.PacketCodec
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.CustomPayload$Id
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.BlockPos
 */
package com.mp3player.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StopCustomDiscPayload(BlockPos pos) implements CustomPacketPayload
{
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"stop_custom_disc");
    public static final CustomPacketPayload.Type<StopCustomDiscPayload> PACKET_ID = new CustomPacketPayload.Type(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, StopCustomDiscPayload> CODEC = StreamCodec.composite((StreamCodec)BlockPos.STREAM_CODEC, StopCustomDiscPayload::pos, StopCustomDiscPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}

