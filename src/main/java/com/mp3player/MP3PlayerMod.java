package com.mp3player;

import com.mp3player.network.BindDiscPayload;
import com.mp3player.network.ClearDiscPayload;
import com.mp3player.network.GlobalPlayPayload;
import com.mp3player.network.GlobalPlayClientPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.StopCustomDiscPayload;
import com.mp3player.network.FileTransferStartPayload;
import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.network.RequestTrackPayload;
import com.mp3player.network.RequestTrackClientPayload;
import com.mp3player.network.ShareRequestPayload;
import com.mp3player.network.ShareResponsePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MP3PlayerMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // Регистрация пакетов
        PayloadTypeRegistry.playC2S().register(BindDiscPayload.ID, BindDiscPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearDiscPayload.PACKET_ID, ClearDiscPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GlobalPlayPayload.PACKET_ID, GlobalPlayPayload.CODEC);
        
        PayloadTypeRegistry.playS2C().register(PlayCustomDiscPayload.ID, PlayCustomDiscPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCustomDiscPayload.PACKET_ID, StopCustomDiscPayload.CODEC);
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

        // Обработка привязки песни к пластинке
        ServerPlayNetworking.registerGlobalReceiver(BindDiscPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(payload.discId());
                if (id != null) {
                    net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(id);
                    if (item != null && item.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                        
                        // Ищем нужную пластинку в инвентаре игрока
                        int slot = -1;
                        for (int i = 0; i < context.player().getInventory().size(); i++) {
                            ItemStack invStack = context.player().getInventory().getStack(i);
                            if (invStack.isOf(item)) {
                                slot = i;
                                break;
                            }
                        }

                        if (slot != -1) {
                            // Забираем 1 штуку
                            context.player().getInventory().removeStack(slot, 1);
                            
                            // Создаем кастомную пластинку с названием
                            ItemStack stack = new ItemStack(item);
                            NbtCompound compound = new NbtCompound();
                            compound.putString("CustomMp3", payload.trackName());
                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
                            stack.set(DataComponentTypes.CUSTOM_NAME, net.minecraft.text.Text.literal("§eПластинка: §f" + payload.trackName()));
                            
                            context.player().getInventory().offerOrDrop(stack);
                            context.player().sendMessage(net.minecraft.text.Text.literal("§aПесня успешно записана на пластинку!"), false);
                        } else {
                            context.player().sendMessage(net.minecraft.text.Text.literal("§cУ вас нет нужной пластинки в инвентаре!"), false);
                        }
                    }
                }
            });
        });

        // Обработка глобального проигрывания
        ServerPlayNetworking.registerGlobalReceiver(GlobalPlayPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity sender = context.player();
                GlobalPlayClientPayload broadcast = new GlobalPlayClientPayload(sender.getUuid(), payload.trackName(), payload.position(), payload.range(), payload.volume());
                
                for (net.minecraft.server.network.ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(p, broadcast);
                }
            });
        });

        // Обработка очистки пластинки
        ServerPlayNetworking.registerGlobalReceiver(ClearDiscPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                int slot = -1;
                for (int i = 0; i < context.player().getInventory().size(); i++) {
                    ItemStack invStack = context.player().getInventory().getStack(i);
                    if (invStack != null && invStack.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                        NbtComponent customData = invStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                        if (customData.contains("CustomMp3")) {
                            slot = i;
                            break;
                        }
                    }
                }

                if (slot != -1) {
                    ItemStack invStack = context.player().getInventory().getStack(slot);
                    // Убираем 1 штуку (если вдруг их несколько)
                    ItemStack singleDisc = invStack.split(1);
                    
                    // Удаляем кастомную дату и название
                    singleDisc.remove(DataComponentTypes.CUSTOM_DATA);
                    singleDisc.remove(DataComponentTypes.CUSTOM_NAME);
                    
                    context.player().getInventory().offerOrDrop(singleDisc);
                    context.player().sendMessage(net.minecraft.text.Text.literal("§aПластинка очищена!"), false);
                } else {
                    context.player().sendMessage(net.minecraft.text.Text.literal("§cУ вас нет кастомных пластинок в инвентаре!"), false);
                }
            });
        });

        // Обработка передачи файлов
        ServerPlayNetworking.registerGlobalReceiver(FileTransferStartPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
                if (target != null) {
                    ServerPlayNetworking.send(target, payload);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FileTransferChunkPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
                if (target != null) {
                    ServerPlayNetworking.send(target, payload);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FileTransferEndPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
                if (target != null) {
                    ServerPlayNetworking.send(target, payload);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ShareRequestPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.target());
                if (target != null) {
                    ServerPlayNetworking.send(target, payload);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ShareResponsePayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity originalSender = context.server().getPlayerManager().getPlayer(payload.originalSender());
                if (originalSender != null) {
                    ServerPlayNetworking.send(originalSender, payload);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(com.mp3player.network.RequestTrackPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.owner());
                if (target != null) {
                    ServerPlayNetworking.send(target, payload);
                }
            });
        });
    }
}
