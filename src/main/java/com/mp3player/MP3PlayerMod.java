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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MP3PlayerMod
implements ModInitializer {
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(BindDiscPayload.ID, BindDiscPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearDiscPayload.PACKET_ID, ClearDiscPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GlobalPlayPayload.PACKET_ID, GlobalPlayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayCustomDiscPayload.ID, PlayCustomDiscPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCustomDiscPayload.PACKET_ID, StopCustomDiscPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(JukeboxSyncPayload.ID, JukeboxSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GlobalPlayClientPayload.PACKET_ID, GlobalPlayClientPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FileTransferStartPayload.PACKET_ID, FileTransferStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FileTransferChunkPayload.PACKET_ID, FileTransferChunkPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FileTransferEndPayload.PACKET_ID, FileTransferEndPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FileTransferStartPayload.PACKET_ID, FileTransferStartPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FileTransferChunkPayload.PACKET_ID, FileTransferChunkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FileTransferEndPayload.PACKET_ID, FileTransferEndPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTrackPayload.PACKET_ID, RequestTrackPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RequestTrackClientPayload.PACKET_ID, RequestTrackClientPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShareRequestPayload.PACKET_ID, ShareRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareRequestPayload.PACKET_ID, ShareRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShareResponsePayload.PACKET_ID, ShareResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareResponsePayload.PACKET_ID, ShareResponsePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BindDiscPayload.ID, (payload, context) -> context.server().execute(() -> {
            Item item;
            Identifier id = Identifier.tryParse((String)payload.discId());
            if (id != null && (item = (Item)Registries.ITEM.get(id)) != null && item.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                int slot = -1;
                for (int i = 0; i < context.player().getInventory().size(); ++i) {
                    ItemStack invStack = context.player().getInventory().getStack(i);
                    if (!invStack.isOf(item)) continue;
                    slot = i;
                    break;
                }
                if (slot != -1) {
                    context.player().getInventory().removeStack(slot, 1);
                    ItemStack stack = new ItemStack((ItemConvertible)item);
                    NbtCompound compound = new NbtCompound();
                    compound.putString("CustomMp3", payload.trackName());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("\u00a7e\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430: \u00a7f" + payload.trackName()));
                    context.player().getInventory().offerOrDrop(stack);
                    context.player().sendMessage((Text)Text.literal((String)"\u00a7a\u041f\u0435\u0441\u043d\u044f \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u0430 \u043d\u0430 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0443!"), false);
                } else {
                    context.player().sendMessage((Text)Text.literal((String)"\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u043d\u0443\u0436\u043d\u043e\u0439 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0438 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!"), false);
                }
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(GlobalPlayPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity sender = context.player();
            GlobalPlayClientPayload broadcast = new GlobalPlayClientPayload(sender.getUuid(), payload.trackName(), payload.position(), payload.range(), payload.volume());
            for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                if (!ServerPlayNetworking.canSend((ServerPlayerEntity)p, broadcast.getId())) continue;
                ServerPlayNetworking.send((ServerPlayerEntity)p, (CustomPayload)broadcast);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClearDiscPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            int slot = -1;
            for (int i = 0; i < context.player().getInventory().size(); ++i) {
                NbtComponent customData;
                ItemStack invStack = context.player().getInventory().getStack(i);
                if (invStack == null || !invStack.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE) || !(customData = (NbtComponent)invStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, (Object)NbtComponent.DEFAULT)).copyNbt().contains("CustomMp3")) continue;
                slot = i;
                break;
            }
            if (slot != -1) {
                ItemStack invStack = context.player().getInventory().getStack(slot);
                ItemStack singleDisc = invStack.split(1);
                singleDisc.remove(DataComponentTypes.CUSTOM_DATA);
                singleDisc.remove(DataComponentTypes.CUSTOM_NAME);
                context.player().getInventory().offerOrDrop(singleDisc);
                context.player().sendMessage((Text)Text.literal((String)"\u00a7a\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430 \u043e\u0447\u0438\u0449\u0435\u043d\u0430!"), false);
            } else {
                context.player().sendMessage((Text)Text.literal((String)"\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0445 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043e\u043a \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435!"), false);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferStartPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayerEntity)target, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)target, (CustomPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferChunkPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayerEntity)target, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)target, (CustomPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(FileTransferEndPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayerEntity)target, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)target, (CustomPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ShareRequestPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayerEntity)target, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)target, (CustomPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ShareResponsePayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity originalSender = context.server().getPlayerManager().getPlayer(payload.originalSender());
            if (originalSender != null && ServerPlayNetworking.canSend((ServerPlayerEntity)originalSender, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)originalSender, (CustomPayload)payload);
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(RequestTrackPayload.PACKET_ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.owner());
            if (target != null && ServerPlayNetworking.canSend((ServerPlayerEntity)target, payload.getId())) {
                ServerPlayNetworking.send((ServerPlayerEntity)target, (CustomPayload)payload);
            }
        }));
    }
}

