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

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record JukeboxSyncPayload(BlockPos pos, String title, String artist, ItemStack discStack, int durationSec, boolean isPlaying) implements CustomPacketPayload
{
    public static final Identifier ID_VALUE = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"jukebox_sync");
    public static final CustomPacketPayload.Type<JukeboxSyncPayload> ID = new CustomPacketPayload.Type(ID_VALUE);
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSyncPayload> CODEC = StreamCodec.composite((StreamCodec)BlockPos.STREAM_CODEC, JukeboxSyncPayload::pos, (StreamCodec)ByteBufCodecs.STRING_UTF8, JukeboxSyncPayload::title, (StreamCodec)ByteBufCodecs.STRING_UTF8, JukeboxSyncPayload::artist, (StreamCodec)ItemStack.OPTIONAL_STREAM_CODEC, JukeboxSyncPayload::discStack, (StreamCodec)ByteBufCodecs.INT, JukeboxSyncPayload::durationSec, (StreamCodec)ByteBufCodecs.BOOL, JukeboxSyncPayload::isPlaying, JukeboxSyncPayload::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

