/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.registry.Registries
 *  net.minecraft.server.network.ServerPlayerEntity
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package com.mp3player;

import com.mp3player.network.BindDiscPayload;
import com.mp3player.network.ClearDiscPayload;
import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.network.FileTransferStartPayload;
import com.mp3player.network.GlobalPlayClientPayload;
import com.mp3player.network.GlobalPlayPayload;
import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.RequestTrackClientPayload;
import com.mp3player.network.RequestTrackPayload;
import com.mp3player.network.ShareRequestPayload;
import com.mp3player.network.ShareResponsePayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;

public class MP3PlayerMod
implements ModInitializer {
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(BindDiscPayload.ID, BindDiscPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClearDiscPayload.PACKET_ID, ClearDiscPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GlobalPlayPayload.PACKET_ID, GlobalPlayPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayCustomDiscPayload.ID, PlayCustomDiscPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StopCustomDiscPayload.PACKET_ID, StopCustomDiscPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JukeboxSyncPayload.ID, JukeboxSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GlobalPlayClientPayload.PACKET_ID, GlobalPlayClientPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FileTransferStartPayload.PACKET_ID, FileTransferStartPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FileTransferChunkPayload.PACKET_ID, FileTransferChunkPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FileTransferEndPayload.PACKET_ID, FileTransferEndPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FileTransferStartPayload.PACKET_ID, FileTransferStartPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FileTransferChunkPayload.PACKET_ID, FileTransferChunkPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FileTransferEndPayload.PACKET_ID, FileTransferEndPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestTrackPayload.PACKET_ID, RequestTrackPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RequestTrackClientPayload.PACKET_ID, RequestTrackClientPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ShareRequestPayload.PACKET_ID, ShareRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ShareRequestPayload.PACKET_ID, ShareRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ShareResponsePayload.PACKET_ID, ShareResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ShareResponsePayload.PACKET_ID, ShareResponsePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BindDiscPayload.ID, (payload, context) -> context.server().execute(() -> {
            Item item;
            Identifier id = Identifier.tryParse((String)payload.discId());
            if (id != null && (item = (Item)BuiltInRegistries.ITEM.getValue(id)) != null && item.components().has(DataComponents.JUKEBOX_PLAYABLE)) {
                int slot = -1;
                for (int i = 0; i < context.player().getInventory().getContainerSize(); ++i) {
                    ItemStack invStack = context.player().getInventory().getItem(i);
                    if (!invStack.is(item)) continue;
                    slot = i;
                    break;
                }
                if (slot != -1) {
                    context.player().getInventory().removeItem(slot, 1);
                    ItemStack stack = new ItemStack((ItemLike)item);
                    CompoundTag compound = new CompoundTag();
                    compound.putString("CustomMp3", payload.trackName());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
                    stack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7e\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430: \u00a7f" + payload.trackName()));
                    context.player().getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
                    context.player().sendSystemMessage((Component)Component.literal((String)"\u00a7a\u041f\u0435\u0441\u043d\u044f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0430 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0443!"));
                } else {
                    context.player().sendSystemMessage((Component)Component.literal((String)"\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u043d\u0443\u0436\u043d\u043e\u0439 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0438 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!"));
                }
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(GlobalPlayPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer sender = context.player();
            GlobalPlayClientPayload broadcast = new GlobalPlayClientPayload(sender.getUUID(), payload.trackName(), payload.position(), payload.range(), payload.volume());
            for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
                if (!ServerPlayNetworking.canSend((ServerPlayer)p, broadcast.type())) continue;
                ServerPlayNetworking.send((ServerPlayer)p, (CustomPacketPayload)broadcast);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClearDiscPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            int slot = -1;
            for (int i = 0; i < context.player().getInventory().getContainerSize(); ++i) {
                CustomData customData;
                ItemStack invStack = context.player().getInventory().getItem(i);
                if (invStack == null || !invStack.getComponents().has(DataComponents.JUKEBOX_PLAYABLE) || !(customData = (CustomData)invStack.getOrDefault(DataComponents.CUSTOM_DATA, (Object)CustomData.EMPTY)).copyTag().contains("CustomMp3")) continue;
                slot = i;
                break;
            }
            if (slot != -1) {
                ItemStack invStack = context.player().getInventory().getItem(slot);
                ItemStack singleDisc = invStack.split(1);
                singleDisc.remove(DataComponents.CUSTOM_DATA);
                singleDisc.remove(DataComponents.CUSTOM_NAME);
                context.player().getInventory().placeItemBackInInventory(singleDisc, net.minecraft.util.Prediction.SERVER_ONLY);
                context.player().sendSystemMessage((Component)Component.literal((String)"\u00a7a\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430 \u043e\u0447\u0438\u0449\u0435\u043d\u0430!"));
            } else {
                context.player().sendSystemMessage((Component)Component.literal((String)"\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0445 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043e\u043a \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!"));
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferStartPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayer)target, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)target, (CustomPacketPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferChunkPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayer)target, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)target, (CustomPacketPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferEndPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayer)target, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)target, (CustomPacketPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ShareRequestPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayer)target, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)target, (CustomPacketPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ShareResponsePayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer originalSender = context.server().getPlayerList().getPlayer(payload.originalSender());
            if (originalSender != null && ServerPlayNetworking.canSend((ServerPlayer)originalSender, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)originalSender, (CustomPacketPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(RequestTrackPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.owner());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayer)target, payload.type())) {
                ServerPlayNetworking.send((ServerPlayer)target, (CustomPacketPayload)payload);
            }
        }));
    }
}

