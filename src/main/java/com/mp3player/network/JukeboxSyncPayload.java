/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.RegistryByteBuf
 *  net.minecraft.network.codec.PacketCodec
 *  net.minecraft.network.codec.PacketCodecs
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.CustomPayload$Id
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.BlockPos
 */
package com.mp3player.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record JukeboxSyncPayload(BlockPos pos, String title, String artist, ItemStack discStack, int durationSec, boolean isPlaying) implements CustomPayload
{
    public static final Identifier ID_VALUE = Identifier.of((String)"mp3player", (String)"jukebox_sync");
    public static final CustomPayload.Id<JukeboxSyncPayload> ID = new CustomPayload.Id(ID_VALUE);
    public static final PacketCodec<RegistryByteBuf, JukeboxSyncPayload> CODEC = PacketCodec.tuple((PacketCodec)BlockPos.PACKET_CODEC, JukeboxSyncPayload::pos, (PacketCodec)PacketCodecs.STRING, JukeboxSyncPayload::title, (PacketCodec)PacketCodecs.STRING, JukeboxSyncPayload::artist, (PacketCodec)ItemStack.OPTIONAL_PACKET_CODEC, JukeboxSyncPayload::discStack, (PacketCodec)PacketCodecs.INTEGER, JukeboxSyncPayload::durationSec, (PacketCodec)PacketCodecs.BOOLEAN, JukeboxSyncPayload::isPlaying, JukeboxSyncPayload::new);

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

