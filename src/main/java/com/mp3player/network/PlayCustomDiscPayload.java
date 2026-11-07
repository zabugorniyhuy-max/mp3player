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
 *  net.minecraft.util.math.BlockPos
 */
package com.mp3player.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PlayCustomDiscPayload(BlockPos pos, String trackName) implements CustomPayload
{
    public static final CustomPayload.Id<PlayCustomDiscPayload> ID = new CustomPayload.Id(Identifier.of((String)"mp3player", (String)"play_custom_disc"));
    public static final PacketCodec<RegistryByteBuf, PlayCustomDiscPayload> CODEC = PacketCodec.tuple((PacketCodec)BlockPos.PACKET_CODEC, PlayCustomDiscPayload::pos, (PacketCodec)PacketCodecs.STRING, PlayCustomDiscPayload::trackName, PlayCustomDiscPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

