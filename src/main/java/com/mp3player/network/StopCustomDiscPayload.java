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

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record StopCustomDiscPayload(BlockPos pos) implements CustomPayload
{
    public static final Identifier ID = Identifier.of((String)"mp3player", (String)"stop_custom_disc");
    public static final CustomPayload.Id<StopCustomDiscPayload> PACKET_ID = new CustomPayload.Id(ID);
    public static final PacketCodec<RegistryByteBuf, StopCustomDiscPayload> CODEC = PacketCodec.tuple((PacketCodec)BlockPos.PACKET_CODEC, StopCustomDiscPayload::pos, StopCustomDiscPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}

