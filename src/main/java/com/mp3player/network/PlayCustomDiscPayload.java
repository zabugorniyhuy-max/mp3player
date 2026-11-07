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

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayCustomDiscPayload(BlockPos pos, String trackName) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<PlayCustomDiscPayload> ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"mp3player", (String)"play_custom_disc"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayCustomDiscPayload> CODEC = StreamCodec.composite((StreamCodec)BlockPos.STREAM_CODEC, PlayCustomDiscPayload::pos, (StreamCodec)ByteBufCodecs.STRING_UTF8, PlayCustomDiscPayload::trackName, PlayCustomDiscPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

