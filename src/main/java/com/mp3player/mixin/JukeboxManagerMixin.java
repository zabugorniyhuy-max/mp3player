package com.mp3player.mixin;

import com.mp3player.network.JukeboxSyncPayload;
import com.mp3player.network.PlayCustomDiscPayload;
import com.mp3player.network.StopCustomDiscPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxManager;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxManager.class)
public class JukeboxManagerMixin {
    @Shadow
    private BlockPos pos;

    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void onStartPlaying(WorldAccess world, RegistryEntry<JukeboxSong> songEntry, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(this.pos);
            if (blockEntity instanceof JukeboxBlockEntity jukebox) {
                ItemStack stack = jukebox.getStack();
                NbtComponent customData;
                if (stack != null && !stack.isEmpty() && (customData = (NbtComponent)stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)).copyNbt().contains("CustomMp3")) {
                    String mp3 = customData.copyNbt().getString("CustomMp3").orElse("");
                    String title = mp3;
                    String artist = "Неизвестен";
                    if (mp3.contains(" - ")) {
                        String[] p = mp3.split(" - ", 2);
                        artist = p[0].trim();
                        title = p[1].replace(".wav", "").replace(".mp3", "").trim();
                    } else {
                        title = mp3.replace(".wav", "").replace(".mp3", "").trim();
                    }
                    JukeboxSyncPayload sync = new JukeboxSyncPayload(this.pos, title, artist, stack, -1, true);
                    for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, this.pos)) {
                        if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                            ServerPlayNetworking.send(p, sync);
                        }
                        if (ServerPlayNetworking.canSend(p, PlayCustomDiscPayload.ID)) {
                            ServerPlayNetworking.send(p, new PlayCustomDiscPayload(this.pos, mp3));
                        }
                    }
                    ci.cancel();
                    return;
                }

                String title = "Пластинка";
                String artist = "Minecraft";
                int dur = 180;
                if (songEntry != null) {
                    JukeboxSong song = songEntry.value();
                    if (song != null) {
                        dur = (int)song.lengthInSeconds();
                        String desc = song.description().getString();
                        if (desc.contains(" - ")) {
                            String[] p = desc.split(" - ", 2);
                            artist = p[0].trim();
                            title = p[1].trim();
                        } else {
                            title = desc;
                        }
                    }
                } else if (stack != null && !stack.isEmpty()) {
                    title = stack.getName().getString();
                }

                JukeboxSyncPayload sync = new JukeboxSyncPayload(this.pos, title, artist, stack != null ? stack : ItemStack.EMPTY, dur, true);
                for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, this.pos)) {
                    if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                        ServerPlayNetworking.send(p, sync);
                    }
                }
            }
        }
    }

    @Inject(method = "stopPlaying", at = @At("HEAD"))
    private void onStopPlaying(WorldAccess world, BlockState state, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            JukeboxSyncPayload stopSync = new JukeboxSyncPayload(this.pos, "", "", ItemStack.EMPTY, 0, false);
            for (ServerPlayerEntity p : PlayerLookup.tracking(serverWorld, this.pos)) {
                if (ServerPlayNetworking.canSend(p, JukeboxSyncPayload.ID)) {
                    ServerPlayNetworking.send(p, stopSync);
                }
                if (ServerPlayNetworking.canSend(p, StopCustomDiscPayload.PACKET_ID)) {
                    ServerPlayNetworking.send(p, new StopCustomDiscPayload(this.pos));
                }
            }
        }
    }
}
